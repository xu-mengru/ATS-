# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Internal HR recruitment position management system (ATS) — CRUD + approval workflow + statistics for job positions, with Excel bulk import. Spring Boot 3.3 + Java 17 backend, Vue 3 + Element Plus frontend.

## Commands

### Backend (Spring Boot + Maven)

```bash
./mvnw compile                       # compile only
./mvnw clean test                    # run all tests (12 tests)
./mvnw -Dtest=PositionServiceTest test  # single test class
./mvnw spring-boot:run               # start dev server (port 8080)
```

### Frontend (Vue 3 + Vite)

```bash
cd ats-frontend
npm install                          # first time only
npm run dev                          # dev server on port 3000
npm run build                        # production build
```

### MySQL Mode

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Architecture

### Backend (`src/main/java/com/hr/ats/`)

Standard layered architecture:

- **`controller/`** — `PositionController` maps to `/webapi/positions`. Full CRUD + Excel upload/download + approval submit/approve/reject + stats + auto-close. Uses `@Valid` on DTOs, returns `Result<T>`.
- **`service/`** — `PositionService` interface + `PositionServiceImpl`. All writes are `@Transactional`. Uses Spring Data JPA `Specification` for dynamic keyword + status filtering. Approval flow: `submitForReview()` → `approvePosition()` / `rejectPosition()`. Statistics returns status counts + 6-month trend.
- **`repository/`** — `PositionRepository` extends `JpaRepository` + `JpaSpecificationExecutor`. Added `countByStatus()`, `countByCreateTimeBetween()`, `findByStatus()`.
- **`dto/`** — `PositionDTO` (with `@NotBlank` + `@Pattern` for status validation) and `PositionExcelDTO` (EasyExcel `@ExcelProperty`).
- **`entity/`** — `Position` JPA entity (table `t_position`). Fields: id, title, description, status, expireDate, createTime, updateTime. Status defaults to `DRAFT`. `@PrePersist`/`@PreUpdate` handle timestamps. `isExpired()` helper.
- **`common/`** — `Result<T>` (unified `{code, message, data}`), `ResultCode` constants, `BusinessException` (caught by `GlobalExceptionHandler`).
- **`config/`** — `WebConfig` (CORS), `PositionScheduler` (hourly auto-close expired positions).

### Frontend (`ats-frontend/src/`)

- **`api/request.js`** — Axios instance (`baseURL: /webapi`), response interceptor shows `ElMessage.error` on non-200 codes.
- **`api/position.js`** — All API functions: CRUD, upload, download template, submit/approve/reject, stats, auto-close.
- **`router/index.js`** — Five routes: list, create, edit, detail, dashboard.
- **`styles/design-system.css`** — Design tokens (colors, spacing, shadows, typography), component classes, responsive breakpoints.
- **`views/PositionList.vue`** — Main list with stats cards, search/filter, table, pagination, approval action handlers.
- **`views/PositionForm.vue`** — Shared create/edit form with status radio buttons, expireDate picker, Markdown preview.
- **`views/PositionDetail.vue`** — Detail view with breadcrumb, meta info, markdown-it rendered description.
- **`views/Dashboard.vue`** — Statistics page with ECharts pie chart (status distribution) + line chart (6-month trend).
- **`components/PositionTable.vue`** — Table with status tags, expire date column, context-sensitive action buttons (view/edit/delete/submit/approve/reject).
- **`components/FileUpload.vue`** — Upload dialog with template download + drag-and-drop.

### Database

H2 in-memory (`jdbc:h2:mem:atsdb;MODE=MySQL`). Schema + seed data from `schema.sql` + `data.sql`. JPA `ddl-auto: none` — schema is SQL-managed. MySQL available via `application-mysql.yml` profile.

### Key Patterns

- **Status lifecycle**: `DRAFT` → `REVIEWING` → `PUBLISHED` or `REJECTED` → (resubmit) → `REVIEWING`. `CLOSED` is terminal. Auto-close via scheduler when `expireDate` passes.
- **Unified response**: Every controller returns `Result<T>`. Errors go through `GlobalExceptionHandler`.
- **Excel import**: Row-by-row via `PositionExcelListener` (EasyExcel). Errors collected per-row, reported back with line numbers.
- **Pagination**: Controller receives 1-based `page`, converts to 0-based `PageRequest`. Clamped to `page >= 1`, `1 <= size <= 100`.
- **Frontend error handling**: Global interceptor handles errors. Components only manage success paths.

### Encoding

Windows requires explicit UTF-8 configuration. Never remove these:
- `.mvn/jvm.config`: `-Dfile.encoding=UTF-8`
- `application.yml`: `spring.sql.init.encoding: UTF-8`, `server.servlet.encoding.force: true`
- All source files must be UTF-8 encoded
