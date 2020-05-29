echo '--start build--'
npm run build

echo '-delete resources file-'
rm -rf ../easy_log_ui/src/main/resources/templates/*
rm -rf ../easy_log_ui/src/main/resources/static/*

echo '--move file to ui resource--'
mv ./dist/index.html ../easy_log_ui/src/main/resources/templates/
mv ./dist/* ../easy_log_ui/src/main/resources/static
