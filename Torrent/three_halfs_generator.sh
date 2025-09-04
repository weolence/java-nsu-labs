#!/bin/bash

# Размеры частей (4GB = 4096MB, делим на 3 части)
PART1_SIZE=1365   # Первая треть (примерно)
PART2_SIZE=1365   # Вторая треть (примерно)
PART3_SIZE=1366   # Третья треть (примерно)

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

# Создание файла 1: первая треть - данные, остальное - нули
create_file1() {
    echo "Создание file1 (первая треть - данные, остальное - нули)..."
    
    # Копируем первую треть исходного файла
    dd if=original_file of=file1 bs=1M count=$PART1_SIZE status=progress 2>/dev/null
    
    # Добавляем нули до полного размера 4GB
    dd if=/dev/zero bs=1M count=$((4096 - PART1_SIZE)) >> file1 status=progress 2>/dev/null
    
    echo "file1 создан."
}

# Создание файла 2: вторая треть - данные, остальное - нули
create_file2() {
    echo "Создание file2 (вторая треть - данные, остальное - нули)..."
    
    # Создаем файл из нулей размером с первую треть
    dd if=/dev/zero of=file2 bs=1M count=$PART1_SIZE status=progress 2>/dev/null
    
    # Добавляем вторую треть из исходного файла
    dd if=original_file of=file2 bs=1M skip=$PART1_SIZE seek=$PART1_SIZE count=$PART2_SIZE status=progress 2>/dev/null
    
    # Добавляем нули до полного размера 4GB
    dd if=/dev/zero bs=1M count=$PART3_SIZE >> file2 status=progress 2>/dev/null
    
    echo "file2 создан."
}

# Создание файла 3: первые 2/3 - нули, последняя треть - данные
create_file3() {
    echo "Создание file3 (первые 2/3 - нули, последняя треть - данные)..."
    
    # Создаем нули для первых двух третей
    dd if=/dev/zero of=file3 bs=1M count=$((PART1_SIZE + PART2_SIZE)) status=progress 2>/dev/null
    
    # Добавляем последнюю треть из исходного файла
    dd if=original_file of=file3 bs=1M skip=$((PART1_SIZE + PART2_SIZE)) seek=$((PART1_SIZE + PART2_SIZE)) count=$PART3_SIZE status=progress 2>/dev/null
    
    echo "file3 создан."
}

# Проверка результатов
verify_files() {
    echo "Проверка созданных файлов..."
    
    # Проверка размеров
    echo "Размеры файлов:"
    ls -lh original_file original_file.torrent file1 file2 file3 | awk '{print $5, $9}'
    
    # Проверка содержимого file1 (первые несколько байт должны быть данные)
    echo -n "Первые байты file1 (должны быть данные): "
    hexdump -n 8 -C file1 | head -1
    
    # Проверка содержимого file2 (середина файла должна быть данные)
    echo -n "Средние байты file2 (должны быть данные): "
    dd if=file2 bs=1M skip=$PART1_SIZE count=1 2>/dev/null | hexdump -n 8 -C | head -1
    
    # Проверка содержимого file3 (конец файла должен быть данные)
    echo -n "Последние байты file3 (должны быть данные): "
    tail -c 8 file3 | hexdump -C
}

# Основная программа
main() {
    echo "Начало выполнения скрипта..."
    
    create_original_file
    create_torrent_file
    create_file1
    create_file2
    create_file3
    verify_files
    
    echo "Все операции завершены успешно!"
}

# Запуск основной программы
main "$@"
