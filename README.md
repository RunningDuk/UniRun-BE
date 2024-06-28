# UniRun
한이음 **UniRun** 프로젝트 **BE** 레포지토리입니다. 

## Convention
### Git Workflow
`main` : 배포 코드가 있는 브랜치
- `develop` : 개발 브랜치
  - `feature/xxx` : 새로운 기능 개발
  - `docs/xxx` : README 등 문서 작업하는 브랜치
  - `refactor/xxx` : 코드 스타일 수정 및 리팩토링을 위한 브랜치
  - `chore/xxx` : 설정 및 기타 작업을 위한 브랜치
- `hotfix` : main에서 버그를 수정할 브랜치
### Branch Naming 
- `develop`
  - `feature/issue-4`
  - `docs/issue-1`
  - `refactor/issue-8`
  - `chore/issue-6`
### Commit Convention
- **feat**: 기능 추가
- **fix**: 버그 수정
- **refactor**: 코드 리팩토링
- **style**: 코드 스타일 수정
- **docs**: 문서 작업
- **chore**: 프로젝트 설정 파일 수정
