# 3dDoofAffe
A repository for storing multiple sets of data in one file. Named after its header bytes (0x3dD00fAffe).
The descriptor may contain anything you want. It is stored as a UTF-8 string in the file.
The content (not the descriptor) is automatically checksummed with CRC-32.

##How to store data in a file

The following code sample creates a DoofAffe archive, and stores the string "Yo World" with the descriptor "text/plain" in the archive.

```java
Path outputPath = Paths.get("hw.da");
OutputStream os = Files.newOutputStream(outputPath);
DoofAffeOutput archiveOutput = new DoofAffeOutput(os);
archiveOutput.writeEntry("text/plain", "Yo World".getBytes("UTF-8"));
archiveOutput.close();
```

##How to retrieve data from a DoofAffe archive

The following code sample reads the first entry of the previously created archive and prints its content.

```java
Path inputPath = Paths.get("hw.da");
InputStream inputStream = Files.newInputStream(inputPath);
DoofAffeInput archiveInput = new DoofAffeInput(inputStream);
Entry<String, byte[]> entry = archiveInput.readEntry();
String s = new String(entry.getValue(), "UTF-8");
System.out.println(s);
archiveInput.close();
```
