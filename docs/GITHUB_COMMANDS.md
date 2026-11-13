# Git quick start
```bash
cd StudyConnect
git init
git add .
git commit -m "chore: initial StudyConnect project skeleton"
git branch -M main
git remote add origin https://github.com/<your-org>/studyconnect.git
git push -u origin main

# each member
git checkout -b feature/<your-area>
# ...work, then small commits...
git add -p
git commit -m "feat: <what you did>"
git push -u origin feature/<your-area>
# open a Pull Request on GitHub, ask a teammate to review, then merge
```
