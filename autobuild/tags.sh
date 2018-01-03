if [ "$TRAVIS_BRANCH" = "master" ]; then
    TAG="release"
else
    TAG="prerelease-$TRAVIS_BRANCH"
fi

git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis"

git remote add release "https://${GH_TOKEN}@github.com/8VM71/TT_Mobile.git"
git push -d release "$TAG"
git tag -d "$TAG"
git tag -a "$TAG" -m "[Autogenerated] Travis build $TRAVIS_BUILD_NUMBER pushed a tag."
git push release --tags