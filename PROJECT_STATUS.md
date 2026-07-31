# 42Vault — Статус проекта (актуально на 31.07.2026)

## 🎯 Цель
Разработать криптокошелёк с функцией принудительной блокировки активов на базе Gem Wallet.

## ✅ Выполнено
- Форкнут репозиторий Gem Wallet.
- Создан модуль `vault` в `core/gemstone/src/vault/`:
  - `mod.rs`
  - `lock_manager.rs`
  - `ntp_client.rs`
  - `time_validator.rs`
- Модуль `vault` отключён в `lib.rs` (закомментирован) для сохранения работоспособности базового кода.
- Восстановлены оригинальные файлы `Cargo.toml` и `lib.rs` из репозитория Gem Wallet.
- Добавлены недостающие зависимости в `Cargo.toml` (`uniffi`, `gem_client`).
- Создан Android-слой:
  - `LockStorage.kt`
  - `SecurityGuard.kt`
  - `VaultViewModel.kt`
  - `VaultScreen.kt`
- Vault интегрирован в навигацию (четвёртая вкладка).
- Добавлен GitHub Actions workflow для сборки APK.

## ❌ Текущие проблемы
- Сборка в CI падает с ошибками, не связанными с нашим кодом:
  - Конфликт версий `num_bigint` (0.4.8 и 0.5.1).
  - Ошибки размера `str` в `keystore`.
  - Отсутствие `uniffi` в некоторых модулях (хотя он добавлен в `Cargo.toml`).
- Возможно, нужна специфическая версия Rust или настройки окружения.

## 🧩 Следующие шаги (после восстановления контекста)
1. Попробовать собрать локально через WSL для детальной отладки.
2. Либо обновить зависимости до совместимых версий.
3. Либо временно закомментировать проблемные части (например, `keystore`), чтобы проверить работу вкладки Vault.

## 📂 Ключевые файлы и папки
- `core/gemstone/src/vault/` — наш модуль (отключён).
- `android/app/src/main/java/com/gemwallet/app/vault/` — Android-слой.
- `android/app/src/main/kotlin/com/gemwallet/android/ui/navigation/` — навигация.
- `core/gemstone/Cargo.toml` — зависимости.
- `.github/workflows/build.yml` — CI сборка.

## 🔗 Ссылка на репозиторий
https://github.com/BIGMAZIXXI/42Vault