#!/bin/bash

# Script to keep the GitHub repository up to date
# This will add all changes, commit, and push to the remote repository

echo "🔄 Updating GitHub repository..."

# Add all changes
echo "📝 Adding all changes..."
git add .

# Check if there are any changes to commit
if git diff-staged --quiet; then
    echo "✅ No changes to commit. Repository is already up to date."
    exit 0
fi

# Get commit message from user or use default
if [ -z "$1" ]; then
    COMMIT_MSG="Update: $(date '+%Y-%m-%d %H:%M:%S')"
else
    COMMIT_MSG="$1"
fi

# Commit changes
echo "💾 Committing changes with message: $COMMIT_MSG"
git commit -m "$COMMIT_MSG"

# Push to remote
echo "🚀 Pushing to GitHub..."
git push origin main

if [ $? -eq 0 ]; then
    echo "✅ Successfully updated GitHub repository!"
else
    echo "❌ Failed to push to GitHub. Please check your connection and try again."
    exit 1
fi
