# Database Migration Commands

Set of commands to migrate database to new version.

## 1. Rename DPMS table

Table will be renamed to `user_dpms`.

```postgresql
ALTER TABLE dpms RENAME TO user_dpms;
```