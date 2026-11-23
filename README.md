# Library Book Management System - Open-Ended Lab (OEL)

Java Swing desktop application using JDK 17+ and standard Java libraries only.

## What it does

- Add books with unique ID, title, author, and default **Available** status.
- View and search books by ID, title, author, or status.
- Select a row to edit ID, title, author, and status.
- Remove selected books (with confirmation).
- Set **Available**, **Issued**, or **Reserved**.
- Enforce required fields and unique IDs; report errors without crashing.
- Save records automatically to `data/library.properties` between launches.

## Requirements

- JDK 17 or newer (`java`, `javac`, `javadoc` on PATH).
- Any Java IDE (IntelliJ IDEA, Eclipse, or NetBeans). No external dependencies.
- Git for commit history and GitHub upload.

## Run on Windows

Open Command Prompt **in the project folder** and execute:

```bat
scripts\run.bat
```

## Run on Linux / macOS

```sh
bash scripts/run.sh
```

## Run unit tests

Windows: `scripts\test.bat`

Linux/macOS: `bash scripts/test.sh`

## Regenerate Javadoc

Windows: `scripts\docs.bat`

Linux/macOS: `bash scripts/docs.sh`

Then open `docs/javadoc/index.html` in a browser.

## Open in an IDE

IntelliJ IDEA: `File > Open` the project folder; choose JDK 17+; mark `src` as Sources Root; run `edu.oel.library.Main`.

Eclipse: Create a new Java project with the same folder and existing source folder `src`, set Java 17+, run class `Main`.

NetBeans: Create a Java with Ant project (or Java project with existing sources); point the source folder to `src`, set `Main Class` to `edu.oel.library.Main`.

Set a breakpoint in `LibraryService.addBook`, use Debug, add a book, and inspect the model/map for IDE-debug evidence.

## Project structure

- `src/edu/oel/library/` - application source code
- `tests/edu/oel/library/` - dependency-free service tests
- `scripts/` - build, run, test, and Javadoc scripts
- `docs/javadoc/` - generated HTML API documentation
- `evidence/` - real application screenshots, test output, Git log/status/diff
- `data/` - local persistence folder (book records ignored by Git)

## Git + GitHub

The ZIP includes a local Git repository with meaningful commits. To publish it, create **your own empty public or private GitHub repository** and run from inside this folder:

```sh
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git branch -M main
git push -u origin main
```

Replace both placeholders with your repository details. GitHub authentication is required; **a remote push has not been performed** by this package. Use `git status`, `git log --oneline --graph`, and `git diff HEAD~1 HEAD` for your viva.

## Workflow notes

- Statuses: Available, Issued, Reserved.
- Search is case-insensitive.
- Book ID: 1-20 characters, letters/digits/hyphen/underscore (e.g. `BK-001`).
- Title and author: non-empty after trimming; tabs/newlines rejected.
- No mock data are inserted on normal launch.
- Data file path can be changed for testing: `-Dlibrary.data.path=/path/to/file.properties`.
- Close the application normally; saving occurs immediately after each change.

## Academic submission

Read `evidence/FINAL_SUBMISSION_CHECKLIST.md` before submitting. The OEL handout requires **two group members**, a printed report, a PDF + ZIP on GCR, a GitHub repository, and an independent viva. Put real student details in the report cover before printing. Do not claim an IDE debug session or GitHub upload before performing them.
