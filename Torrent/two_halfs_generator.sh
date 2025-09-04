#!/bin/bash

# Создание исходного файла 4GB со случайными данными
create_original_file() {
    echo "Создание исходного файла размером 4GB со случайными данными..."
    dd if=/dev/urandom of=original_file bs=1M count=4096 status=progress 2>/dev/null
    echo "Исходный файл создан."
}

# Создание .torrent файла без трекеров
create_torrent_file() {
    echo "Создание .torrent файла без трекеров..."
    
    # Проверяем наличие утилиты mktorrent
    if ! command -v mktorrent &> /dev/null; then
        echo "Ошибка: утилита mktorrent не установлена."
        echo "Установите её с помощью:"
        echo "  Ubuntu/Debian: sudo apt install mktorrent"
        echo "  CentOS/RHEL: sudo yum install mktorrent"
        echo "  Fedora: sudo dnf install mktorrent"
        exit 1
    fi
    
    # Создаем .torrent файл без трекеров
    mktorrent -a "" -o original_file.torrent original_file
    
    echo ".torrent файл создан: original_file.torrent"
}

# Создание файла с нулями в первой половине
create_file1() {
    echo "Создание file1 (первые 2GB - нули, вторые 2GB - данные из исходного файла)..."
    
    # Создаем первую половину (2GB нулей)
    dd if=/dev/zero of=file1 bs=1M count=2048 status=progress 2>/dev/null
    
    # Добавляем вторую половину из исходного файла
    dd if=original_file of=file1 bs=1M skip=2048 seek=2048 count=2048 status=progress 2>/dev/null
    
    echo "file1 создан."
}

# Создание файла с нулями во второй половине
create_file2() {
    echo "Создание file2 (первые 2GB - данные из исходного файла, вторые 2GB - нули)..."
    
    # Копируем первую половину исходного файла
    dd if=original_file of=file2 bs=1M count=2048 status=progress 2>/dev/null
    
    # Добавляем 2GB нулей в конец
    dd if=/dev/zero bs=1M count=2048 >> file2 status=progress 2>/dev/null
    
    echo "file2 создан."
}

# Основная программа
main() {
    echo "Начало выполнения скрипта..."
    
    create_original_file
    create_torrent_file
    create_file1
    create_file2
    
    echo "Все операции завершены успешно!"
    echo "Созданные файлы:"
    ls -lh original_file original_file.torrent file1 file2
}

# Запуск основной программы
main "$@"
