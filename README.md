# PandaEntus
身内鯖の Discord Bot

Windows / macOS / Linux で動作します。Java 25 以降が必要です。

必要になったみたいな機能を暇になったときに追加する

# 起動
`mvn clean package` でビルドして、`target/PandaEntus-1.3-jar-with-dependencies.jar` を Bot 用のフォルダに置きます。
起動スクリプトの見本は `start.sh` (Linux / macOS) と `start.bat` (Windows) です。

# 外部ツール
| ツール | 使う機能 | 用意の方法 |
----|----|----
| yt-dlp | YouTube リンクのリアクション、TikTok、Instagram | 自動で `tools/` にダウンロードして、定期的に更新します |
| ffmpeg | YouTube リンクのリアクション (mp3 への変換) | Windows / Linux は自動で `tools/` にダウンロードして、定期的に更新します ([FFmpeg-Builds](https://github.com/FFmpeg-Builds/FFmpeg-Builds))。macOS は `brew install ffmpeg` などで入れてください |
| Open JTalk | `!ps` (読み上げ) | 各自で入れて、config.yml の `OpenJTalk` を設定します |
| Tesseract | `!ocr` | Windows は同梱されています。Linux / macOS は各自で入れてください |

自動ダウンロードは、config.yml の `Tools.AutoUpdate: false` で止められます。自分で用意したものを使うときは、`Tools.YtDlp.Path` と `Tools.FFmpeg.Path` を指定します。

# Command
| コマンド | 内容 | 権限 |
----|----|---- 
| !uptime | サーバーの起動時間を確認します | 全員 |
| !pool | 投票を作成します。 | 全員 |
| !profile | アカウント作成日を確認します | 全員 |
| !server | サーバー作成日を確認します | 全員 |
| !version | Bot のバージョン(ビルド日)を確認します | 全員 |
| !ojichat | おじさんになります | 全員 |
| !vanish | 通話終了時のリストから隠れます | 全員 |
| !stats | Bot の情報を表示します | 全員 |
| !icon | 指定したユーザーのアイコンを返します | 全員 |

## ネタ枠
| コマンド | 内容 | 権限 |
----|----|---- 
| !akinator | 無能です。終わらないです。 | 全員 |
| !dab | \<​o\/ | 全員 |
| !kv | 通話に参加してる状態で使ってね | 全員 |
| !text-gen | 指定したテキストの画像を作成します [Cooltext](https://ja.cooltext.com/) | 全員 |

## Music Bot (不安定)
| コマンド | 内容 | 権限 |
----|----|---- 
| !play | YouTube にある音楽を再生します | 全員 |
| !loop | 再生中の音楽をリピートします | 全員 |
| !skip | 再生中の音楽をスキップします | 全員 |

## 他のコマンド
| コマンド | 内容 | 権限 |
----|----|---- 
| =x+x | 電卓です。はい。 | 全員 |
| YouTube リンク | リアクションを押すとファイルを取得します | 全員 |

## 管理者向け
| コマンド | 内容 | 権限 |
----|----|----
| !shutdown | Bot をシャットダウンさせます | 管理者 |
| !init | Bot で必要なカテゴリ、チャンネルを自動生成します | 管理者 |
| !add-vc | 指定した数だけボイスチャンネルを増やします (最大50個) | 管理者 |
| !setgame | プレイ中のゲームを設定します | 管理者 |
| !sendchat | 指定したチャンネルに Bot からメッセージを送信します | 管理者 |
| !disk | ディスクの空き状況を確認します | 管理者 |
| !free | メモリの空き状況を確認します | 管理者 |
| !emojis | サーバーに登録されている絵文字を表示します(コンソール) | 管理者 |
| !pool set | キーと値を設定します | 管理者 |
