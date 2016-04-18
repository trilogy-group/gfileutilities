#GFileUtilities

The GFileUtilities is written in Java 1.8. The idea is to create a handy tool for file operations in Java.
Currently it supports only encryption/decryption for files or folders as well as message authentication. 

##Encryption/Decryption/MAC

Every file is encrypted and a message authentication code is attached to the file as well. In the output file name
the .gfile is attached as an extension. During the decryption you can verify if the encrypted part of the message 
through the message authentication code. The encryption 
is being done by using AES-256 with CBC and PKCS5Padding. For the message authentication HmacSHA256 is used. Furthermore,
during the encryption the file name of the file is being saved as well and it is contained inside the encryption part of the message.

##Message Format
The format of the resulting encrypted message is the following:
```
|SALT|IV|ENCRYPTED MESSAGE|HMAC|
```

The format of the ENCRYPTED MESSAGE is the following:
```
|Number of Files|1st File name Length|1st File name|1st File size|1st data|....|Nth File name Length|Nth File name|Nth File size|Nth data|
```

###Usage:

Build:
```
gradle build
```

Encryption of a file:
```
./gfileutilities -e -f foo.txt -o foo -p password
```

Encryption of a folder/subfolders:
```
./gfileutilities -e -m folder -o foo -p password
```

Encryption of multiple files:
```
./gfileutilities -e -m foo1.txt foo2.txt -o foo -p password
```


Decryption of a file without checking the HMAC: 
```
./gfileutilities -d -f foo.gfile -p password -o folder
```

Decryption of multiple files without checking the HMAC: 
```
./gfileutilities -d -m foo1.gfile foo2.gfile -p password -o folder
```

Decryption of a file with checking the HMAC: 
```
./gfileutilities -x -f foo.gfile -p password -o folder
```

Verify only the HMAC:
```
./gfileutilities -v -f foo.gfile -p password
```
