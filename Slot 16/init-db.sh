#!/bin/bash

# Tìm đường dẫn sqlcmd
if [ -f /opt/mssql-tools18/bin/sqlcmd ]; then
    SQLCMD="/opt/mssql-tools18/bin/sqlcmd -C"
elif [ -f /opt/mssql-tools/bin/sqlcmd ]; then
    SQLCMD="/opt/mssql-tools/bin/sqlcmd"
else
    SQLCMD="sqlcmd -C"
fi

echo "=========================================================="
echo "Đang đợi SQL Server khởi động hoàn tất..."
echo "=========================================================="

READY_PASS=""
for i in {1..60}; do
    if $SQLCMD -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT 1" > /dev/null 2>&1; then
        READY_PASS="$MSSQL_SA_PASSWORD"
        break
    elif $SQLCMD -S localhost -U sa -P "sa" -Q "SELECT 1" > /dev/null 2>&1; then
        READY_PASS="sa"
        break
    fi
    sleep 2
done

if [ -z "$READY_PASS" ]; then
    echo "LỖI: SQL Server không phản hồi sau thời gian chờ."
    exit 1
fi

echo "SQL Server đã sẵn sàng! Tiến hành kiểm tra và khởi tạo database..."

# Kiểm tra nếu DB chưa được khởi tạo thì tiến hành tạo DB và bảng
if [ ! -f /var/opt/mssql/.db_initialized ]; then
    echo "1. Đặt mật khẩu sa = 'sa' (để khớp với application.properties của đề thi) và tạo database MSS301_2026_PE_MEDIA..."
    $SQLCMD -S localhost -U sa -P "$READY_PASS" -Q "
        ALTER LOGIN sa WITH PASSWORD = 'sa', CHECK_POLICY = OFF;
        IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'MSS301_2026_PE_MEDIA')
        BEGIN
            CREATE DATABASE [MSS301_2026_PE_MEDIA];
        END
    "

    echo "2. Chạy file create_users_sql.txt để tạo bảng users..."
    $SQLCMD -S localhost -U sa -P "sa" -i /app/create_users_sql.txt

    echo "3. Chạy file create_posts_sql.txt để tạo bảng posts..."
    $SQLCMD -S localhost -U sa -P "sa" -i /app/create_posts_sql.txt

    touch /var/opt/mssql/.db_initialized
    echo "=========================================================="
    echo "Khởi tạo thành công Database MSS301_2026_PE_MEDIA!"
    echo "Bảng users và posts đã sẵn sàng."
    echo "Tài khoản đăng nhập:"
    echo "  - Username: sa"
    echo "  - Password: sa (hoặc YourStrong@Passw0rd)"
    echo "  - Port: 1433"
    echo "=========================================================="
else
    echo "Database đã được khởi tạo trước đó. Bỏ qua bước tạo bảng."
fi
