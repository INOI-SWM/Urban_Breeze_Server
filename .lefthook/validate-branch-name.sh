#!/bin/bash

branch="$(git rev-parse --abbrev-ref HEAD)"
IFS='/' read -ra PARTS <<< "$branch"

types="feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert|design"
valid_types=(${types//|/ })

if [ ${#PARTS[@]} -ne 3 ]; then
  echo -e "\n❌ 오류: 브랜치 이름 '$branch'이(가) 올바른 형식이 아닙니다."
  echo "형식: <타입>/<JIRA 키>/<케밥케이스 브랜치이름>"
  echo "예시: feat/INOI-123/add-login-button"
  exit 1
fi

type="${PARTS[0]}"
jira="${PARTS[1]}"
name="${PARTS[2]}"

is_valid_type=false
for t in "${valid_types[@]}"; do
  if [[ "$type" == "$t" ]]; then
    is_valid_type=true
    break
  fi
done

if [ "$is_valid_type" = false ]; then
  echo -e "\n❌ 오류: 브랜치 타입 '$type'은(는) 허용되지 않습니다."
  echo "허용된 타입: ${valid_types[*]}"
  exit 1
fi

if [[ ! "$jira" =~ ^INOI-[0-9]+$ ]]; then
  echo -e "\n❌ 오류: Jira 키 '$jira'는 'INOI-숫자' 형식이어야 합니다."
  echo "예시: INOI-123"
  exit 1
fi

if [[ ! "$name" =~ ^[a-z0-9]+(-[a-z0-9]+)*$ ]]; then
  echo -e "\n❌ 오류: 브랜치 이름 '$name'은 케밥 케이스여야 합니다."
  echo "조건: 소문자, 숫자, 하이픈(-)만 사용하며, 하이픈으로 단어를 구분합니다."
  echo "예시: add-login-button"
  exit 1
fi

echo "✅ 브랜치 이름 검사 통과: $branch"
exit 0