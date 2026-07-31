# 42Vault — Статус проекта (актуально на 31.07.2026, вечер)

## 🎯 Цель
Разработать криптокошелёк с функцией принудительной блокировки активов на базе Gem Wallet.

## ✅ Выполнено
- Форкнут репозиторий Gem Wallet.
- Создан модуль `vault` в `core/gemstone/src/vault/` (отключён в `lib.rs`).
- Восстановлены оригинальные файлы `Cargo.toml` и `lib.rs` из репозитория Gem Wallet.
- Создан Android-слой: `LockStorage.kt`, `SecurityGuard.kt`, `VaultViewModel.kt`, `VaultScreen.kt`.
- Vault интегрирован в навигацию (четвёртая вкладка).
- Добавлен GitHub Actions workflow для сборки APK.
- Настроен WSL (Ubuntu) на локальной машине.
- Установлены Rust, cargo-ndk, just, Java 17, Android SDK.
- Проект клонирован в WSL.

## ⚠️ Текущая проблема
- Сборка в WSL падает с ошибкой `SDK location not found`. Нужно корректно указать путь к Android SDK в `local.properties` или через `ANDROID_HOME`.
- Возможно, SDK установлен не полностью (не хватает платформ и build-tools).

## 🧩 Следующие шаги (после восстановления контекста)
1. Установить недостающие компоненты Android SDK (platforms, build-tools, ndk) через `sdkmanager`.
2. Убедиться, что `local.properties` содержит правильный `sdk.dir`.
3. Попробовать собрать APK снова.
4. Если сборка пройдёт — протестировать приложение на устройстве.

## 📂 Ключевые файлы и папки
- `core/gemstone/src/vault/` — наш модуль (отключён).
- `android/app/src/main/java/com/gemwallet/app/vault/` — Android-слой.
- `android/app/src/main/kotlin/com/gemwallet/android/ui/navigation/` — навигация.
- `core/gemstone/Cargo.toml` — зависимости.
- `.github/workflows/build.yml` — CI сборка.

## 🔗 Ссылка на репозиторий
https://github.com/BIGMAZIXXI/42Vault

## 📌 Команды для следующего шага (в PowerShell)
# Установка компонентов SDK
wsl -d Ubuntu -- bash -c "yes | sdkmanager 'platforms;android-35' 'build-tools;35.0.0' 'ndk;26.1.10909125'"

# Проверка пути SDK
wsl -d Ubuntu -- bash -c "ls -la /usr/lib/android-sdk"

# Создание local.properties и сборка
wsl -d Ubuntu -- bash -c "echo 'sdk.dir=/usr/lib/android-sdk' > ~/42Vault/android/local.properties && cd ~/42Vault/android && ./gradlew assembleDebug"