#!/bin/bash

# Chạy script tự động tạo database và bảng trong background
/app/init-db.sh &

# Chạy SQL Server ở foreground (tiến trình chính của container)
exec /opt/mssql/bin/sqlservr
