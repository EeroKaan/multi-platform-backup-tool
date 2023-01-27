# Multi Platform Backup Tool (MPBT)

The MPBT application was developed by Eero Kaan to manage backup tasks of specialized WebDev environments across multiple machines. Currently, servers with Plain, Plesk or LXC Container devspace architectures are supported.

## Features

- Support for specialized target machine environments
  - Plain target machine (Files and database directly accessible) 
  - Plesk target machine (Files and database are managed by a Plesk Installation) 
  - LXC Container machine (Files and database are stored in separate Containers on the target machine) 
- Backup of the accompanying database of the specified file destination (WordPress, Drupal, Magento, etc.)
- Ability to perform Backups in one of the following modes:
  - Local machine – Local machine
  - Local machine – Remote machine
  - Remote machine – Local machine
  - Remote machine – Remote machine
- Resilient rsync file transfer between machines (Resuming after broken connections)
- Support for multiple simultaneous backup jobs via Session-Identifiers

## Usage

`java -jar mpbt.jar [-h] [-d] [-b] [-e <arg>] [-i <arg>] [-o <arg>] [-lxcn <arg>] [-dbh <arg>] [-dbn <arg>] [-dbu '<arg>'] [-dbp '<arg>']`

<table>
    <tr>
        <td>-h, --help</td>
        <td>Display help documentation</td>
    </tr>
    <tr>
        <td>-d, --debug</td>
        <td>Enable debug mode</td>
    </tr>
    <tr>
        <td>-b, --backup</td>
        <td>Start a Backup job</td>
    </tr>
    <tr>
        <td>-e, --environment &lt;arg&gt;</td>
        <td>The source environment<br>[plain, plesk, lxc]</td>
    </tr>
    <tr>
        <td>-i, --input &lt;arg&gt;</td>
        <td>The source directory to be backed up</td>
    </tr>
    <tr>
        <td>-o, --output &lt;arg&gt;</td>
        <td>The output directory for the backup to be placed in</td>
    </tr>
    <tr>
        <td>-lxcn, --lxcContainerName &lt;arg&gt;</td>
        <td>The name of the LXC Container containing the source files</td>
    </tr>
    <tr>
        <td>-dbh, --databaseHost &lt;arg&gt;</td>
        <td>The database server host<br>(Source Machine / Container is Reference Point)</td>
    </tr>
    <tr>
        <td>-dbn, --databaseName &lt;arg&gt;</td>
        <td>The name of the database</td>
    </tr>
    <tr>
        <td>-dbu, --databaseUser &lt;arg&gt;</td>
        <td>The name of the database user</td>
    </tr>
    <tr>
        <td>-dbp, --databasePassword &lt;arg&gt;</td>
        <td>The password of the database user</td>
    </tr>
</table>

## Requirements

- **Java Runtime** On the system running MPBT
- **mysql / mariadb-client** On target machine when using Database Backup functionality
- **Prepared SSH Key authentication** To all desired remote machines

## License

[Permitted use under the MIT licence](https://choosealicense.com/licenses/mit/)