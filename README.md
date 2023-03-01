# Multi Platform Backup Tool (MPBT)

The MPBT application enables you to manage backups on different kinds of webdev environments across multiple machines. Currently, servers with `plain`, `plesk` or `lxc` container architectures are supported.

## Features

- Support for backing up and restoring directories, MySQL databases and Elasticsearch instances
- Support for specialized machine environments
    - Plain machines (data is directly accessible)
    - Plesk machines (data is managed by a Plesk instance)
    - LXC container machines (data is stored in separate LXC containers)
- Support for push/pull backup/restore via `context` and `TARGET` settings:
    - Local machine → Local machine
    - Local machine → Remote machine
    - Remote machine → Local machine
    - Remote machine → Remote machine
- Support for multiple simultaneous jobs
- Resilient rsync file transfer between machines (auto-resume after connection error)

## Usage

```
java -jar mpbt.jar [OPTIONS...] TARGET
java -jar mpbt.jar [OPTIONS...] /home/user/mybackups
java -jar mpbt.jar [OPTIONS...] user@remotemachine.com:/home/user/mybackups
```

<table>
    <tr>
        <td colspan="2"><b>General</b></td>
    </tr>
    <tr>
        <td>--help</td>
        <td>Display help documentation</td>
    </tr>
    <tr>
        <td>--debug</td>
        <td>Enable debug mode</td>
    </tr>
    <tr>
        <td>--mode &lt;arg&gt;</td>
        <td>The mode to use MPBT with<br>[backup, restore]</td>
    </tr>
    <tr>
        <td>--environment &lt;arg&gt;</td>
        <td>The source environment<br>[plain, plesk, lxc]</td>
    </tr>
    <tr>
        <td>--context &lt;arg&gt;</td>
        <td>The machine from which hostnames and paths are viewed from</td>
    </tr>
    <tr>
        <td colspan="2"><b>Backup/Restore Types</b></td>
    </tr>
    <tr>
        <td>--directory</td>
        <td>Enable backing up/restoring a directory</td>
    </tr>
    <tr>
        <td>--database</td>
        <td>Enable backing up/restoring a MySQL database</td>
    </tr>
    <tr>
        <td>--elasticsearch</td>
        <td>Enable backing up/restoring a Elasticsearch instance</td>
    </tr>
    <tr>
        <td colspan="2"><b>Directory specific parameters</b></td>
    </tr>
    <tr>
        <td>--directoryPath &lt;arg&gt;</td>
        <td>The directory to backup/restore</td>
    </tr>
    <tr>
        <td colspan="2"><b>Database specific parameters</b></td>
    </tr>
    <tr>
        <td>--dbHost &lt;arg&gt;</td>
        <td>The database server host</td>
    </tr>
    <tr>
        <td>--dbName &lt;arg&gt;</td>
        <td>The name of the database</td>
    </tr>
    <tr>
        <td>--dbUser '&lt;arg&gt;'</td>
        <td>The name of the database user</td>
    </tr>
    <tr>
        <td>--dbPassword '&lt;arg&gt;'</td>
        <td>The password of the database user</td>
    </tr>
    <tr>
        <td colspan="2"><b>Elasticsearch specific parameters</b></td>
    </tr>
    <tr>
        <td>--LOREM</td>
        <td>LOREM</td>
    </tr>
</table>

## Requirements

- `openjdk-<VERSION>-jre` On the system running MPBT
- `mysql-client / mariadb-client` On target machine when using database backup/restore functionality
- `Prepared SSH key authentication` To all desired remote machines

## License

[Permitted use under the MIT licence](https://choosealicense.com/licenses/mit/)