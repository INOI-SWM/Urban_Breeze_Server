#!/bin/bash

# Cron Job 설정 스크립트

echo "Setting up log backup cron jobs..."

# 스크립트 실행 권한 부여
chmod +x scripts/log-backup.sh

# Docker 볼륨 확인
echo "Checking Docker volumes..."
if ! docker volume ls | grep -q "urbanbreeze-logs"; then
    echo "Warning: urbanbreeze-logs volume not found. Please ensure containers are running."
fi

# Cron Job 추가 (백업 스크립트 사용)
(crontab -l 2>/dev/null; echo "# UrbanBreeze Log Backup (Using Backup Script)") | crontab -
(crontab -l 2>/dev/null; echo "0 */6 * * * /home/ubuntu/scripts/log-backup.sh prod s3://urbanbreeze-logs/prod/logs/ >> /var/log/urbanbreeze-backup.log 2>&1") | crontab -
(crontab -l 2>/dev/null; echo "0 */6 * * * /home/ubuntu/scripts/log-backup.sh dev s3://urbanbreeze-logs/dev/logs/ >> /var/log/urbanbreeze-backup.log 2>&1") | crontab -

echo "Cron jobs added:"
echo "- Every 6 hours: Log backup for prod and dev (Docker volume based)"
echo "- Log file: /var/log/urbanbreeze-backup.log"
echo "- Working directory: /home/ubuntu"

# Docker 볼륨 상태 확인
echo "Docker volume status:"
docker volume ls | grep urbanbreeze || echo "No urbanbreeze volumes found"

# Cron Job 확인
echo "Current cron jobs:"
crontab -l
