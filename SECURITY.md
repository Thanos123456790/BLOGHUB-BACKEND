# Security Policy

## Reporting Vulnerabilities
Please report security vulnerabilities to the project maintainers privately. Do not open public issues for security bugs.

## Secrets Rotation (VLN-19)
Real Clerk API keys were previously committed to `bloghub_frontend_fixed/.env`. **Those keys must be rotated immediately:**

1. Log into the [Clerk Dashboard](https://dashboard.clerk.com)
2. Navigate to **API Keys → Rotate**
3. Update your deployment environment variables with the new keys
4. To scrub the git history (required if the repo is or was ever public):

```bash
# Install git-filter-repo
pip install git-filter-repo

# Remove the .env file from all history
git filter-repo --path frontend/.env --invert-paths

# Force-push to all remotes
git push origin --force --all
git push origin --force --tags
```

> **Note:** All collaborators must re-clone the repository after a history rewrite.
