FROM --platform=linux/amd64 mcr.microsoft.com/mssql/server:2022-latest

USER root
WORKDIR /app

# Copy các file DDL SQL và script khởi tạo vào container
COPY create_users_sql.txt /app/create_users_sql.txt
COPY create_posts_sql.txt /app/create_posts_sql.txt
COPY init-db.sh /app/init-db.sh
COPY entrypoint.sh /app/entrypoint.sh

# Cấp quyền thực thi cho các script
RUN chmod +x /app/init-db.sh /app/entrypoint.sh

# Chuyển về user mssql mặc định của image
USER mssql

EXPOSE 1433

ENTRYPOINT ["/app/entrypoint.sh"]
