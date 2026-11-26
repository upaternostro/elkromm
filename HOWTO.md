# How to...

Short recipes for the changes that come up most often. They are checklists, not
tutorials: read the code of a similar, existing command first (a good way to see
everything a change touches is to look at the commit that added it).

## ...add a command

1.  Add the API into `ElkrommFacade`, with its Javadoc and `@since`
1.  Add the command in `ElkronCommand`, with a `null` packet class for the moment
    (each constant refers to its packet class, which does not exist yet)
1.  Create the DTO
1.  Create the serializer, and register it in `ElkrommFactory` (`getXxxSerializer()`),
    so that the facade and the emulator can find it
1.  Define the packet in `packet/` and update `ElkronCommand` with the right class
1.  Implement the API into `ElkrommFacadeImpl`
1.  Eventually modify `Model`, if the emulator has to keep the new data
1.  Teach the emulator about the command, in `ClientConnection.run()`:
    -   a command that *reads* data gets a `case` that serializes the data of the `Model` and
        queues it with `enqueuePayload()`
    -   a command that *writes* data (or performs an action) gets a `case` in the list of
        the commands that answer with the SYN only and delegate to `processPayload()`
1.  Add the related `case` to `ClientConnection.processPayload()`, for the commands that
    write or act: deserialize the payload, update the `Model` and, when the data are covered
    by the checksums, call `Model.computeChecksum()`, otherwise the checksums answered
    by the emulator will be stale
1.  Write the tests:
    -   the serializer unit test (DTO &rarr; bytes &rarr; DTO)
    -   the functional test of the facade against the emulator
        (`ElkrommFacadeFunctionalTest`, with the helpers in `ElkrommTestUtils`):
        this is usually the biggest part of the change
    -   for a read command, a round trip in `RoundTripIT` (against a real panel, see
        [round-trip integration tests](README.md#round-trip-integration-tests) and `elkron-it.properties`): it reads, deserializes
        and serializes again, and the bytes must match
1.  Document the command:
    -   a row in the command table of `PROTOCOL.md` and `PROTOCOL-ITA.md`
    -   an entry in `CHANGELOG.md`
    -   if the emulator needs new defaults, see [add an emulator property](#add-an-emulator-property)
1.  Before pushing, `mvn verify` (unit tests *and* integration tests, with the panel
    reachable) and the Javadoc without warnings

## ...add an emulator property

1.  Add the two constants in `emulator.Config`, both with their Javadoc: `K_*` for the key
    (a `String.format` pattern if the property applies to several objects) and `D_*` for the default
1.  Add the getter in `Config`, with its Javadoc. An enumeration is read with
    `Enum.valueOf("<PREFIX>_" + value)`, so that the file holds the name of the constant
    **without** its prefix (`COT_PARTITIONS` becomes `PARTITIONS`): the default is written
    the same way
1.  Use the getter in `Model` or wherever the emulator needs the value
1.  Add the row to the table of the properties in the `README.md`
    (key, default, notes: for an enumeration, list its values)
1.  Add or update the tests of `Config`, and mention the property in the `CHANGELOG.md`
    (an invalid enumeration name makes the getter throw `IllegalArgumentException`,
    an invalid number is logged and replaced by the default)

## ...release a version

1.  Make sure that `CHANGELOG.md` has the section of the version and that the unit tests,
    the integration tests (`mvn verify`, with the panel reachable) and the Javadoc are clean
1.  Check that the dependencies are releases: the build refuses `-SNAPSHOT`
    dependencies when deploying (in particular `mock-ipc`)
1.  Set the final version in `pom.xml` (drop the `-SNAPSHOT`) and **commit it**: the tag must point
    to a commit that holds the final version
1.  Add the compare/tag links at the bottom of `CHANGELOG.md` (only now, not before) and commit it too
1.  Make sure that nothing is left out of the commits: `git status` must be empty. To be sure that
    the local build is the one the workflow will see, stash everything (`git stash -u`, which also
    takes the untracked files), run `mvn clean install` (without the local panel configuration, which is stashed too,
    the integration tests do not run: that is why they are the first step of this list, run on the
    working copy as it is), push if all is green and only then `git stash pop`.
    A version published on Maven Central cannot be taken back, so a surprise found by the workflow
    costs a version number
1.  Tag that commit `vX.Y.Z` and push the commits *and* the tag: the `Release` workflow builds, signs, uploads to Maven Central and
    creates the GitHub release with the jars. Launched by hand it does a dry run (build and
    signature, nothing uploaded), which is useful to check the secrets
1.  Set the next `-SNAPSHOT` version in `pom.xml` and commit it
