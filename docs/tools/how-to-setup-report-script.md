# OpenPAI Report Script Sample Implementation
After deploy OpenPAI in your organization as a running cluster, OpenPAI cluster admins need to monitor the usage of the cluster, gather and track information like resource utilization, VC usage distribution, Job counts and status, etc. These set of information can not only improve the operating of the running cluster, but also help OpenPAI advancing the system in the end.

To help Admins jump start fast, starting from 2019 April release, we provided a report script sample implementation. This document will provide information about how to deploy and use this script to generate report. Administrators can reference the sample implementation and customize their own report scripts based on their needs. For practices or codes that you think might be common to share, please don't hesitate to contribute back to OpenPAI community.

## About the sample script

The sample script will generate 4 reports which includes 4 csv files `vc.csv`, `job.csv`, `alert.csv` and `raw_job.csv`.

### vc.csv

This file will tell you who/what vc used most/least of cluster resource, it have following columns:

* user: username in OpenPai cluster
* vc: VC name in OpenPai cluster
* cpu: number of vcore allocated for jobs
* mem: how many of memory(MB) allocated for jobs
* gpu: number of GPU card allocated for jobs

### job.csv

This file will tell you uses' job statistic, this including the final status, job count and job resources, it have following columns:

* user: username in OpenPai cluster
* vc: VC name in OpenPai cluster
* total job info: sum of all jobs
* successful job info: those finished and exit code is 0
* failed job info: those finished and exit code is not 0
* stopped job info: those stopped by user
* running job info: those running

The job info is group of following subcolumns:

* count: job count of this category
* elapsed time: job running time of this category
* cpu second: how much vcore-second used by jobs of this category
* memory second: how much memory(MB)-second used by jobs of this category
* gpu second: how much gpu-second used by jobs of this category

### alert.csv

This file will tell you what alerts was triggered in your cluster, the script can generate this report even if you didn't set an alert manager. Because the Prometheus service will delete data that's old enough, in default setup, it only retains 15 days of data, you may want to extend the retaintion date if you want an accurate number in montly report.

The file have following columns:

* alert name: alert name defined in prometheus
* instance: where this alert was triggered
* start: start time of this alert
* durtion: how much time(seconds) this alert lasts

### raw_job.csv

This file is a detailed job info, the `job.csv` can be deemed as aggreated statistic of this file.

The file have following columns:

* user: username in OpenPai cluster
* vc: VC name in OpenPai cluster
* job name: job name in OpenPai cluster
* start time: when the job got started, it will be a timestamp value
* finished time: when the job finished, it will be a timestamp value, if the job is still running, this will have value 0
* waiting time: how much time(second) this job is in waiting status before running, this include waiting time of the retries. If the job is still running, this will have value 0
* running time: how much time this job is in running status, this include retries
* retries: the retry count of this job
* status: the status of the job, it could be `WAITING`, `RUNNING`, `SUCCEEDED`, `STOPPED`, `FAILED` and `UNKNOWN`
* exit code: the exit code of the job, if the job is still in running, it will be value `N/A`
* cpu allocated: how many vcore allocated to the job, this include the vcore allocated to app master
* memory allocated: how much memory(MB) allocated to the job, this include the memory allocated to app master
* gpu allocated: how many gpu card allocated to the job

## Prerequisite

You should prepare a node that have access to OpenPai services, the script will need to access hadoop-resource-manager, framework-launcher and Prometheus deployed by OpenPai. This node do not need to have much memory resource and do not need to have GPU cards. You only need to make sure this node will not restart frequently. Usually the master node of the OpenPai cluster is a good choice.

After you choose a node, please make sure you have following software installed:

* python3
* requests library

If your node is ubuntu node, you can install these software using following commands:

``` sh
sudo apt-get install -y python3 python3-pip
pip3 install requests
```

## How to Setup

The [script](../../src/tools/reports.py) has two actions, `refresh` and `report`.

The `refresh` action will tries to collect data from hadoop-resource-manager and framework-launcher, and save the data in sqlite3 DB for future process. The script needs to save data because hadoop-resource-manager will not retain job info too long, if we do not fetch them and save somewhere, we will not be able to generate correct report. We recommend admin run this script every 10 minutes using CRON job.

The `report` action will query data about vc usage and job statistic from sqlite3 DB and generate vc/job/raw_job reports, it will also get data from Prometheus to generate alert reports. You can execute this action whenever you want the reports.

First, log into the node you choose, put the [script](../../src/tools/reports.py) somewhere, for example, I put it in directory `/home/core/report`, edit the crontab using

``` sh
crontab -e
```

It will prompt an editor with some documentation, you will need to paste following content at the end of the file

``` crontab
*/10 * * * * python3 /home/core/report/reports.py refresh -y $yarn_url -p $prometheus_url -l $launcher_url -d /home/core/report/cluster.sqlite >> /home/core/report/cluster.log 2>&1
```

Please replace `$yarn_url`, `$prometheus_url` and `$launcher_url` with your cluster value, they are should be like `http://$master:8088`, `http://$master:9091` and `http://$master:9086` respectively where `$master` is the IP/hostname of your OpenPai master, please also make sure they are in one line. It is a good practice to execute the command before put into crontab.

After finished, you should save and exit the editor. You can then execute

``` sh
crontab -l
```

to view your current crontab. It should showing what you edited.

All available arguments and meanings can be viewed by executing script with `-h` arguments.

The script will automatically delete old data, by default, it will retain 6 months of data. If this is too large for you, for example, if you only want to retain 1 months of data, you can add `-r 31` to above command to tell script delete data that's older than 31 days.

Whenever you want an report, you can log into that node again and execute following command

``` sh
python3 /home/core/report/reports.py report -y $yarn_url -p $prometheus_url -l $launcher_url -d /home/core/report/cluster.sqlite
```

By default, the script will generate a monthly report, which means it will query data from one month ago until now and use these data to generate the reports, you can change the time range using `--since` and `--until` argument, for example, if you want the reports from one month ago and until one week ago, you can add these arguments:

``` sh
--since `date --date='-1 month' +"%s"` --until `date --date='-1 week' +"%s"`
```
