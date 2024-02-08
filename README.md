# wvsh

## Build

Run the following command from the root of the repository to make a debug
build:

```shell
./gradlew assembleDebug
```

Making a release build is similar, but requires environment variables to be set
to indicate how to sign the APK:

```shell
STORE_FILE='...' STORE_PASSWORD='...' KEY_ALIAS='...' KEY_PASSWORD='...' ./gradlew assembleRelease
```


## License

[MIT](LICENSE)
