# Multi Platform Backup Tool (MPBT)

The MPBT application enables you to manage backups on different kinds of webdev environments across multiple machines. Currently, servers with `plain`, `plesk` or `lxc` architectures are supported.

## Features

- Support for backing up and restoring directories, MySQL databases and Elasticsearch instances
- Support for different machine environments
    - Plain machines (data is directly accessible)
    - Plesk machines (data is managed by a Plesk instance)
    - LXC container machines (data is stored in separate LXC containers)
- Support for push/pull backup/restore:
    - Local machine → Local machine
    - Local machine → Remote machine
    - Remote machine → Local machine
    - Remote machine → Remote machine
- Support for multiple simultaneous jobs
- Resilient rsync file transfer between machines (auto-resume after connection loss)

## Usage

`java -jar mpbt.jar [OPTIONS...] TARBALL`

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
        <td colspan="2"><b>Job Types</b></td>
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
        <td>--esHost &lt;arg&gt;</td>
        <td>The Elasticsearch master node hostname</td>
    </tr>
    <tr>
        <td>--esIndexPrefix &lt;arg&gt;</td>
        <td>The Elasticsearch index prefix to backup/restore</td>
    </tr>
</table>

## Requirements

- Java Runtime
- `mysql-client` or compatible (e.g. `mariadb-client`)
- Prepared SSH key authentication

## License

[Permitted use under the MIT licence](https://choosealicense.com/licenses/mit/)