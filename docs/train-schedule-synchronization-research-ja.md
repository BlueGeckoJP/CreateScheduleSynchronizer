# Train Schedule同期アイテム 技術調査

調査日: 2026-06-26

## 目的

Create ModのTrain Scheduleを置き換える互換アイテムを追加し、同一の同期IDを持つ時刻表間で内容を共有する機能が実現可能か調査する。

想定する主な動作は次のとおり。

- 新しい時刻表アイテムはCreateのTrain Scheduleと同様に編集・使用できる
- 各アイテムは同期IDを持つ
- 同じ同期IDを持つ時刻表は同じ内容を参照する
- 一つを編集すると、同じIDの時刻表からも更新後の内容を利用できる
- 必要に応じて、列車に投入済みの時刻表も更新対象にする

## 調査対象

このプロジェクトの現在の対象環境:

- Minecraft 1.21.1
- NeoForge 21.1.234
- Java 21
- Kotlin 2.0.0

Create側は、調査時点でMinecraft 1.21.1向けの最新タグである以下を確認した。

- Create 6.0.10
- タグ: `mc1.21.1-6.0.10`
- コミット: `ac0c444d9828da3453ae8cc65338e8de063286fb`

現在のプロジェクトはNeoForge/Kotlinの初期テンプレートに近い状態であり、Createへの依存関係や時刻表実装はまだ追加されていない。

## 結論

実現可能である。

ただし、CreateのTrain Scheduleを完全に置き換え、純正アイテムと同じ操作経路すべてに対応するには、独自アイテムを登録するだけでは足りない。Create 6.0.10には純正Train Scheduleを直接判定・生成する箇所があるため、少数のMixinまたは同等のフックが必要になる。

実装難易度は対象範囲によって異なる。

| 対象 | 実現性 | 補足 |
| --- | --- | --- |
| 同期アイテム同士の内容共有 | 高い | SavedDataとData Componentで実装可能 |
| 純正Schedule GUIの再利用 | 高い | 保存パケットの純正アイテム限定判定への対応が必要 |
| プレイヤーから列車への投入 | 高い | `ScheduleItem`継承で大部分を再利用可能 |
| 車掌ブロックへの投入 | 可能 | 純正アイテム限定判定の拡張が必要 |
| Train Stationの自動時刻表 | 可能 | 純正アイテム限定判定の拡張が必要 |
| 列車から取り外した際の同期ID維持 | 可能 | `ScheduleRuntime.returnSchedule()`への対応が必要 |
| 運行中の列車へのリアルタイム反映 | 可能だが複雑 | 進行位置や待機状態の扱いを仕様化する必要がある |

Createのレジストリ内にある純正アイテムを直接差し替える方法は、他Modとの互換性やCreate更新時の破損リスクが高いため推奨しない。独自アイテムを登録し、レシピ・タグ・必要箇所のMixinで置き換える構成が安全である。

## Create側の時刻表データ構造

### アイテム内のSchedule

Create 6.0.10では、時刻表の内容はItemStackのData Componentに`CompoundTag`として保存される。

```java
public static final DataComponentType<CompoundTag> TRAIN_SCHEDULE = register(
    "train_schedule",
    builder -> builder
        .persistent(CompoundTag.CODEC)
        .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
);
```

したがって、時刻表内容は次の性質を持つ。

- ItemStackとともに永続化される
- クライアントへネットワーク同期される
- `Schedule.write()`で`CompoundTag`へ変換される
- `Schedule.fromTag()`で復元される

独自アイテムでもCreateの`create:train_schedule` Data Componentを保持すれば、CreateのSchedule読み取り処理やツールチップ処理を再利用できる。

### GUIによる編集

`ScheduleItem`を右クリックするとCreateのSchedule GUIが開く。編集結果は`ScheduleEditPacket`でサーバーへ送信され、メインハンドのItemStackに書き戻される。

問題は、パケット処理が次のように純正Train Scheduleだけを許可している点である。

```java
ItemStack mainHandItem = sender.getMainHandItem();
if (!AllItems.SCHEDULE.isIn(mainHandItem))
    return;
```

そのため、独自アイテムでGUIだけを再利用しても、編集結果は保存されない。ここはMixin等で同期アイテムを許可し、さらに同期ストレージを更新する必要がある。

### 列車に投入したSchedule

プレイヤーが車掌へ時刻表を渡す経路では、`ScheduleItemEntityInteraction`がアイテムを`instanceof ScheduleItem`で判定する。

独自アイテムを`ScheduleItem`のサブクラスとして実装すれば、この経路は利用できる。

投入時にはItemStackからScheduleが復元され、次のように列車へ渡される。

```java
train.runtime.setSchedule(schedule, false);
```

その後、元のItemStackは一つ消費される。

### 列車内部のSchedule

`ScheduleRuntime.setSchedule()`は渡された`Schedule`をそのまま保持する。

```java
public void setSchedule(Schedule schedule, boolean auto) {
    reset();
    this.schedule = schedule;
    // ...
}
```

列車の保存時には`ScheduleRuntime`内のScheduleがNBTへ書き込まれる。しかし、元のItemStackや独自の同期IDは保持されない。

つまり、アイテムを列車へ投入した時点で、通常は「同期IDを持つアイテム」から「Schedule内容だけを持つ列車ランタイム」へ変換される。このままでは投入後の列車を同期グループから追跡できない。

### 列車からの取り外し

列車からScheduleを取り外す際、Createは必ず純正Train Scheduleを新規作成する。

```java
ItemStack stack = AllItems.SCHEDULE.asStack();
stack.set(AllDataComponents.TRAIN_SCHEDULE, schedule.write(registries));
discardSchedule();
return stack;
```

そのため、独自アイテムを投入できたとしても、何も変更しなければ取り外し時に純正アイテムへ変わり、同期IDも失われる。

## 純正アイテムに固定されている経路

Create 6.0.10で確認した主な固定箇所は以下のとおり。

| クラス | 処理 | 問題 |
| --- | --- | --- |
| `ScheduleEditPacket` | GUI編集結果の保存 | `AllItems.SCHEDULE.isIn()`で独自アイテムを拒否 |
| `ConductorBlockInteractionBehavior` | 車掌ブロックへの投入 | 純正アイテムだけを許可 |
| `StationBlockEntity.applyAutoSchedule()` | Train Stationの自動時刻表 | 純正アイテムだけを許可 |
| `ScheduleRuntime.returnSchedule()` | 列車から時刻表を返却 | 常に純正アイテムを生成 |
| `ScheduleItemEntityInteraction` | エンティティ車掌への投入 | `ScheduleItem`サブクラスなら対応可能 |

## 推奨アーキテクチャ

### 同期アイテム

独自アイテムを`ScheduleItem`のサブクラスとして登録する。

ItemStackには少なくとも以下を持たせる。

```text
createschedulesynchronizer:sync_id
  UUID

createschedulesynchronizer:revision
  Long

create:train_schedule
  CompoundTag
```

`create:train_schedule`はCreate互換表示・GUI・使用処理のためのローカルキャッシュとして扱う。

### ワールド単位の同期ストレージ

サーバーの`SavedData`に同期IDごとの正式な内容を保存する。

```text
sync_id -> {
  revision,
  schedule_tag,
  updated_at,
  updated_by
}
```

最低限必要なのは`revision`と`schedule_tag`である。`updated_at`と`updated_by`は競合調査や管理コマンドに利用できるため、必要なら追加する。

SavedDataを正本にする理由:

- 同じIDの全ItemStackを毎回検索する必要がない
- チェスト、シュルカーボックス、未ロードチャンク内のアイテムを直接更新しなくてよい
- アイテムを読み込んだ時点で最新内容へ追従できる
- ItemStack複製やコマンド配布後も、同じIDなら同じ内容を参照できる

### 遅延同期

世界中のItemStackを即時走査する方式は採用しない。

以下のタイミングでSavedDataからItemStackのキャッシュを更新する。

- アイテムを右クリックしてGUIを開く前
- 列車または車掌へ渡す前
- Train Stationが自動時刻表を読み取る前
- プレイヤーのインベントリ内にある間の低頻度tick
- ツールチップ表示に必要なクライアント同期時

ItemStackの`revision`がSavedDataより古い場合だけ`create:train_schedule`を更新する。

### 編集処理

同期アイテムの編集結果を受け取った場合:

1. メインハンドの同期IDを取得する
2. 受信したScheduleを検証する
3. SavedDataのScheduleを更新する
4. revisionを増加させる
5. 編集中ItemStackのキャッシュとrevisionを更新する
6. 必要なら同一プレイヤーのインベントリ内にある同IDアイテムも即時更新する

サーバー側でメインハンドと同期IDを確認し、クライアントから送られたIDを無条件に信用しないこと。

## 必要になるMixin候補

### 必須

#### `ScheduleEditPacket.handle()`

- 独自同期アイテムを許可する
- CreateのSchedule GUIから送られた編集結果をSavedDataへ保存する
- 純正Scheduleに対する既存動作は変更しない

#### `ConductorBlockInteractionBehavior`

- `AllItems.SCHEDULE.isIn(itemInHand)`判定を「純正または同期時刻表」へ拡張する
- 列車へ渡す直前にSavedDataから最新Scheduleを解決する

#### `StationBlockEntity.applyAutoSchedule()`

- Train Stationのデポ上にある同期時刻表を許可する
- 適用直前に最新Scheduleを解決する

### 列車からの返却まで互換にする場合

#### `ScheduleRuntime`

Mixinフィールドとして同期元を保持する。

```text
syncId: UUID?
syncRevision: long
```

必要な対応:

- `setSchedule()`時に同期IDを保存
- `write()`時に同期IDを列車NBTへ保存
- `read()`時に同期IDを復元
- `discardSchedule()`時に同期情報もクリア
- `returnSchedule()`時に同期アイテムを生成し、同期IDを復元

ただし、Createの`setSchedule(Schedule, boolean)`にはItemStackが渡されない。同期IDを関連付けるには、その呼び出し元で一時的に情報を渡す、追加インターフェースを設ける、または投入処理自体をフックする必要がある。

## 運行中の列車を同期する場合

運行中の列車を更新対象に含めることも技術的には可能だが、単純なSchedule差し替えでは不十分である。

検討が必要な状態:

- 現在のScheduleエントリ番号
- 現在実行中の待機条件
- 条件の経過時間や進捗
- 現在の目的駅
- 一時停止・完了状態
- 自動Scheduleか手動Scheduleか

推奨する初期仕様は以下のいずれか。

### 案A: 次回投入時だけ反映

すでに列車へ投入済みのScheduleは変更しない。

- 実装が最も安全
- Create本来の運行状態を壊しにくい
- 「アイテム同士の同期」という最小要件を満たせる

### 案B: 現在のエントリ完了後に反映

列車は更新要求を保留し、次のエントリへ進む境界で新しいScheduleへ切り替える。

- 現在の待機条件を途中で破棄しない
- リアルタイム性と安全性の妥協点
- ScheduleRuntimeへの追加状態が必要

### 案C: 即時反映

編集直後に同じ同期IDの全列車へ新しいScheduleを設定する。

- 最も分かりやすい同期動作
- 現在位置と新しいエントリの対応規則が必要
- エントリ削除や並べ替え時の挙動が不安定になりやすい

初期実装では案Aを推奨する。アイテム同期とCreate互換操作を安定させた後、案Bを追加するのが安全である。

## 純正Train Scheduleの置き換え方法

推奨構成:

1. 独自同期時刻表アイテムを登録する
2. 純正Train Scheduleのレシピをデータパックで差し替える
3. 必要なら純正アイテムをクリエイティブタブから非表示にする
4. 純正アイテムから同期時刻表への変換レシピを用意する
5. Create内部の互換判定だけをMixinで拡張する

純正アイテムのレジストリエントリそのものを置換すると、Createや他アドオンが保持する参照との不整合が起きやすい。レシピと利用経路を置き換え、純正アイテム自体は登録されたままにする。

## 実装順序

### フェーズ1: 最小実装

- Create 6.0.10への開発依存を追加
- 同期ID Data Componentを登録
- `ScheduleItem`派生アイテムを登録
- SavedDataを実装
- 右クリックGUIを開く前の読み込み
- `ScheduleEditPacket`保存処理へのMixin
- 同じIDを持つ手持ちアイテム間の同期テスト

### フェーズ2: Create操作互換

- エンティティ車掌への投入
- 車掌ブロックへの投入
- Train Station自動時刻表
- 列車から取り外した際の同期アイテム復元
- 純正レシピの差し替え

### フェーズ3: 運行中同期

- 列車ランタイムへの同期ID保存
- ワールド内の同期対象列車の追跡
- 更新適用タイミングの仕様決定
- 進行状態を維持した更新処理

## 主なテスト項目

- 同じIDを持つ二つのアイテムの片方を編集すると、もう片方が更新される
- 異なるIDのアイテムには変更が伝播しない
- サーバー再起動後も同期内容が保持される
- チェストから取り出した古いrevisionのアイテムが最新化される
- アイテム複製後も同じIDなら同期される
- 純正Scheduleは従来どおり利用できる
- 同期アイテムをエンティティ車掌へ渡せる
- 同期アイテムを車掌ブロックへ渡せる
- Train Stationの自動時刻表として利用できる
- 列車から取り外しても同期IDが失われない
- 空のScheduleや不正なScheduleパケットでSavedDataが壊れない
- 同時編集時の競合規則が一貫している

## 競合規則

初期実装では「サーバーが最後に受理した編集を採用する」でよい。

revisionを利用し、必要なら将来以下を追加できる。

- 編集開始時revisionと保存時revisionが異なる場合の警告
- 古いGUIからの上書き拒否
- 管理者向けの同期ID・revision確認コマンド
- 同期IDの再発行・リンク解除操作

## 参照したCreate公式ソース

- Release: <https://github.com/Creators-of-Create/Create/releases/tag/mc1.21.1-6.0.10>
- `ScheduleItem`: <https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/content/trains/schedule/ScheduleItem.java>
- `ScheduleEditPacket`: <https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/content/trains/schedule/ScheduleEditPacket.java>
- `ScheduleRuntime`: <https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/content/trains/schedule/ScheduleRuntime.java>
- `StationBlockEntity`: <https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/content/trains/station/StationBlockEntity.java>
- `ConductorBlockInteractionBehavior`: <https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/behaviour/interaction/ConductorBlockInteractionBehavior.java>
- `AllDataComponents`: <https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/AllDataComponents.java>

