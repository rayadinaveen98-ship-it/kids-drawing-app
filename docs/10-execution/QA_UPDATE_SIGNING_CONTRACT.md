# QA Update Signing Contract

This contract applies to every user-installable sideloaded QA APK delivered after Content V2.5.

## Package identity

- `applicationId`: `com.navin.kidsdrawing`
- first durable-update baseline: `versionCode 32`
- baseline versionName: `0.6.1-content-v2.5-qa1`
- every later installable APK MUST use a strictly greater `versionCode`.

## Signing identity

User-installable QA APKs MUST be signed with the persistent private QA update key kept outside the public repository.

Expected signer certificate SHA-256:

`5E:DB:6B:22:31:89:98:07:14:64:CA:54:0A:BE:C2:0F:73:4D:D6:E1:6C:C9:4C:F3:38:07:6E:0F:69:FC:13:23`

The private key, alias password and keystore password MUST NOT be committed to this repository.

## CI rule

The `profile` build is intentionally unsigned in repository CI. CI may validate its package identity, permissions and content, but an unsigned/ephemeral CI artifact is **not** the APK handed to the tester.

After exact-head CI is green, the profile APK is signed outside the repository with the persistent QA update key. Before delivery, verify:

1. package name is `com.navin.kidsdrawing`;
2. versionCode is strictly greater than the previous delivered installable APK;
3. signer certificate SHA-256 exactly matches the fingerprint above;
4. APK installs over the immediately previous durable-signed QA APK without uninstalling it.

## Migration note

Older QA APKs were produced with ephemeral CI debug signing identities. Those private keys were runner-local and are no longer available, so they cannot be made compatible retroactively. Moving from an older ephemeral-signed APK to the durable signing baseline may require one final uninstall/install. Once the durable baseline is installed, uninstalling for normal updates is a release-blocking failure.
