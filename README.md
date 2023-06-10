# Multi Platform Backup Tool (MPBT)

MPBT is a versatile command line tool, which enables you to backup and restore environments across multiple machines

## Features

- Support for operations
  - backup
  - restore
- Support for resources
  - directory
  - database
  - elasticsearch
- Support for resources to be containerized with LXC
- Support for all resource locations to be remote. You can even mix and match.
  - local → local
  - local → remote
  - remote → local
  - remote → remote
- Resilient rsync file transfer between machines (auto-resume after connection loss)

## Usage/Examples

```
java -jar mpbt.jar [OPTIONS...] TARBALL
```

### Backup and restore on local machine

```
java -jar mpbt.jar --operation backup --directory --dirPath /var/www/html /home/user/backup-target.tar
java -jar mpbt.jar --operation restore --directory --dirPath /var/www/html /home/user/backup-source.tar
```

### Backup remote directory on _server01_ to another remote location on _server02_

```
java -jar mpbt.jar --operation backup --directory --dirPath user@server01.com:/my/remote/server01/directory user@server02.com:/my/remote/server02/backup-target.tar
```

### Backup directory within local LXC container and database within remote LXC container on _server01_ to remote LXC container on _server02_

```
java -jar mpbt.jar --operation backup --directory --database --dirPath lxc%local-container-name:/my/directory/within/lxc --dbHost user@server01.com:lxc%remote-mysql-container:localhost --dbName myDatabase --dbUser myUser --dbPassword myPassword user@server02.com:lxc%remote-container-name:/my/remote/server02/backup-target.tar
```

## Arguments

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
        <td>Enable debug output</td>
    </tr>
    <tr>
        <td>--operation &lt;arg&gt;</td>
        <td>The operation to use MPBT with<br>[backup, restore]</td>
    </tr>
    <tr>
        <td colspan="2"><b>Job resources</b></td>
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
        <td>--dirPath &lt;arg&gt;</td>
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
        <td>--dbUser &lt;arg&gt;</td>
        <td>The name of the database user</td>
    </tr>
    <tr>
        <td>--dbPassword &lt;arg&gt;</td>
        <td>The password of the database user</td>
    </tr>
    <tr>
        <td colspan="2"><b>Elasticsearch specific parameters</b></td>
    </tr>
    <tr>
        <td>--esHost &lt;arg&gt;</td>
        <td>The Elasticsearch master node hostname</td>
    </tr>
    <tr>
        <td>--esIndexPrefix &lt;arg&gt;</td>
        <td>The Elasticsearch index prefix to backup/restore</td>
    </tr>
</table>

## Requirements

- Java runtime
- MySQL client (e.g. `mariadb-client`)
- Prepared SSH key authentication

## License

[Permitted use under the MIT licence](https://choosealicense.com/licenses/mit/)