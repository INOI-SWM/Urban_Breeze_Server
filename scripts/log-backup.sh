#!/bin/bash

# 로그 백업 스크립트
# 사용법: ./log-backup.sh [환경] [S3경로]
# 예시: ./log-backup.sh prod s3://urbanbreeze-logs/prod/logs/

ENVIRONMENT=${1:-prod}
S3_PATH=${2:-s3://urbanbreeze-logs/prod/logs/}
CONTAINER_NAME="urban-breeze-${ENVIRONMENT}"
VOLUME_NAME="urbanbreeze-logs"

# AWS CLI 환경 변수 설정
export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
export AWS_DEFAULT_REGION=${AWS_DEFAULT_REGION:-ap-northeast-2}

echo "Starting log backup for ${ENVIRONMENT} environment..."

# 컨테이너 상태 확인
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "Container ${CONTAINER_NAME} is not running. Skipping backup."
    exit 0
fi

# Docker 볼륨 확인
if ! docker volume ls | grep -q "${VOLUME_NAME}"; then
    echo "Volume ${VOLUME_NAME} not found. Skipping backup."
    exit 0
fi

# 백업 파일명 생성
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="urbanbreeze_logs_${ENVIRONMENT}_${DATE}.tar.gz"

echo "Creating backup: ${BACKUP_FILE}"

# Docker 볼륨에서 로그 백업 (Docker 볼륨에서 직접 접근)
docker run --rm \
    -v ${VOLUME_NAME}:/logs \
    -v /tmp:/backup \
    alpine sh -c "tar -czf /backup/${BACKUP_FILE} -C /logs ."

if [ $? -eq 0 ]; then
    echo "Backup file created successfully: ${BACKUP_FILE}"
    
    # S3에 업로드
    echo "Uploading to S3: ${S3_PATH}"
    if aws s3 cp "/tmp/${BACKUP_FILE}" "${S3_PATH}"; then
        echo "Backup uploaded to S3 successfully"
        
        # 로컬 백업 파일 삭제
        rm "/tmp/${BACKUP_FILE}"
        echo "Local backup file removed"
        
        # S3에서 오래된 백업 파일 삭제 (8개월 이상)
        echo "Cleaning up old backups..."
        aws s3 ls "${S3_PATH}" | while read -r line; do
            createDate=$(echo $line | awk '{print $1" "$2}')
            createDate=$(date -d"$createDate" +%s)
            olderThan=$(date -d"8 months ago" +%s)
            if [[ $createDate -lt $olderThan ]]; then
                fileName=$(echo $line | awk '{print $4}')
                if [[ $fileName != "" ]]; then
                    aws s3 rm "${S3_PATH}${fileName}"
                    echo "Deleted old backup: ${fileName}"
                fi
            fi
        done
        
    else
        echo "Failed to upload backup to S3"
        exit 1
    fi
else
    echo "Failed to create backup file"
    exit 1
fi

echo "Log backup completed successfully"
