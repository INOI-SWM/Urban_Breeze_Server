#!/bin/bash

commit_msg=$(cat "$1")

types="feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert|design"

clean_message() {
  echo "$1" | sed '/^#/d' | sed '/^\s*$/d'
}

commit_msg=$(clean_message "$commit_msg")

if [[ "$commit_msg" =~ ^($types):\ \[.*\]\ .+ ]]; then
  if [[ ! "$commit_msg" =~ ^($types):\ \[INOI-[0-9]+\]\ .+ ]]; then
    echo "❌ 지라 코드는 '[INOI-숫자]' 형식이어야 합니다."
    exit 1
  fi
elif [[ "$commit_msg" =~ ^($types):\ .+ ]]; then
  # 정상 메시지
  :
else
  echo "❌ 커밋 메시지는 다음 형식을 따라야 합니다: 'type: 설명' (예: feat: 메시지 내용)"
  echo "   사용 가능한 타입: $types"
  exit 1
fi

echo "✅ 커밋 메시지 검사 통과"
exit 0