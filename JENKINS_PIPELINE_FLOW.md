# Jenkins Pipeline Flow Guide

This document summarizes the hands-on deployment flow for this project from Jenkins to EC2, including the health check and the most common fixes discussed during setup.

## 1. What This Pipeline Does

The pipeline in `Jenkinsfile` runs these stages:

1. `Checkout`
   Pull source code from GitHub.
2. `Test`
   Run:
   ```bash
   mvn clean test
   ```
3. `Package`
   Run:
   ```bash
   mvn package -DskipTests
   ```
4. `Deploy`
   Copy the built jar and restart the Spring service:
   ```bash
   cp target/*.jar /opt/cloud-file-processing/app.jar
   sudo systemctl restart cloud-file-processing
   ```
5. `Health Check`
   Wait until the app responds successfully on:
   ```bash
   http://localhost:8080/actuator/health
   ```

## 2. Expected Server Layout On EC2

The pipeline assumes these items already exist on the EC2 instance:

- App jar location:
  ```bash
  /opt/cloud-file-processing/app.jar
  ```
- `systemd` service name:
  ```bash
  cloud-file-processing
  ```
- Environment file:
  ```bash
  /etc/cloud-file-processing/app.env
  ```

## 3. Connect To The EC2 Instance

Use AWS Console:

1. Open `EC2`.
2. Select the instance.
3. Click `Connect`.
4. Choose `EC2 Instance Connect`.
5. Click `Connect`.

Useful first checks:

```bash
whoami
pwd
sudo systemctl status cloud-file-processing
```

## 4. Create Or Verify The App Directories

Run:

```bash
sudo mkdir -p /opt/cloud-file-processing
sudo mkdir -p /etc/cloud-file-processing
sudo chown -R ec2-user:ec2-user /opt/cloud-file-processing
```

If your Jenkins agent writes directly on the same EC2 machine, make sure the Jenkins user can copy the jar into `/opt/cloud-file-processing`.

## 5. Create The Environment File

Create or edit:

```bash
sudo nano /etc/cloud-file-processing/app.env
```

Example content:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://java-demo-db.cnokogu2m09s.ap-southeast-1.rds.amazonaws.com:3306/java_demo_db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=password

APP_AWS_REGION=ap-southeast-1
APP_AWS_S3_BUCKET_NAME=quan-javajdemo-bucket
APP_AWS_SQS_QUEUE_URL=https://sqs.ap-southeast-1.amazonaws.com/051942114572/java-demo-queue

APP_WORKER_POLL_DELAY_MS=5000
```

Important:

- Do not write:
  ```bash
  APP_AWS_SQS_QUEUE_URL=sqs-queue-url: https://sqs.ap-southeast-1.amazonaws.com/051942114572/java-demo-queue
  ```
- The value must be only the URL.
- If using IAM role on EC2, you do not need static AWS keys in the env file.

## 6. Create Or Verify The `systemd` Service

Edit:

```bash
sudo nano /etc/systemd/system/cloud-file-processing.service
```

Example:

```ini
[Unit]
Description=Cloud File Processing Spring Boot App
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/opt/cloud-file-processing
EnvironmentFile=/etc/cloud-file-processing/app.env
ExecStart=/usr/bin/java -jar /opt/cloud-file-processing/app.jar
SuccessExitStatus=143
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Then reload and enable:

```bash
sudo systemctl daemon-reload
sudo systemctl enable cloud-file-processing
sudo systemctl restart cloud-file-processing
```

Verify:

```bash
sudo systemctl status cloud-file-processing
```

## 7. Allow Jenkins To Restart The Service Without Password Prompt

The pipeline uses:

```bash
sudo systemctl restart cloud-file-processing
```

If Jenkins asks for a password, the deploy stage fails and the health check is skipped.

Check the Jenkins runtime user:

```bash
whoami
ps -ef | grep jenkins
```

Then add a sudoers rule safely:

```bash
sudo visudo
```

Add one of these lines depending on `which systemctl`:

```text
jenkins ALL=(ALL) NOPASSWD: /bin/systemctl restart cloud-file-processing, /bin/systemctl status cloud-file-processing
```

or:

```text
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart cloud-file-processing, /usr/bin/systemctl status cloud-file-processing
```

Safer alternative:

```bash
sudo visudo -f /etc/sudoers.d/jenkins
```

Then save and exit.

## 8. How The Health Check Works

This project includes Spring Boot Actuator and exposes the health endpoint:

```bash
http://localhost:8080/actuator/health
```

You can test it manually:

```bash
curl -fsS http://localhost:8080/actuator/health
```

Expected result:

```json
{"status":"UP"}
```

The Jenkins pipeline retries this endpoint for up to about 60 seconds.

If the app does not start, the health check fails.

## 9. Manual Deployment Test On EC2

After Jenkins builds the jar, you can verify deployment manually on the EC2 host:

```bash
ls -l /opt/cloud-file-processing/app.jar
sudo systemctl restart cloud-file-processing
sudo systemctl status cloud-file-processing
curl -fsS http://localhost:8080/actuator/health
```

## 10. Check Logs

Latest 100 log lines:

```bash
sudo journalctl -u cloud-file-processing -n 100 --no-pager
```

Follow logs live:

```bash
sudo journalctl -u cloud-file-processing -f
```

Stop service:

```bash
sudo systemctl stop cloud-file-processing
```

Start service:

```bash
sudo systemctl start cloud-file-processing
```

Restart service:

```bash
sudo systemctl restart cloud-file-processing
```

## 11. Fix For The SQS Queue URL Error

If you see an error like:

```text
InvalidAddressException: The address sqs-queue-url: https://sqs.ap-southeast-1.amazonaws.com/051942114572/java-demo-queue is not valid
```

The issue is usually inside:

```bash
/etc/cloud-file-processing/app.env
```

Correct value:

```bash
APP_AWS_SQS_QUEUE_URL=https://sqs.ap-southeast-1.amazonaws.com/051942114572/java-demo-queue
```

Wrong value:

```bash
APP_AWS_SQS_QUEUE_URL=sqs-queue-url: https://sqs.ap-southeast-1.amazonaws.com/051942114572/java-demo-queue
```

After fixing:

```bash
sudo systemctl daemon-reload
sudo systemctl restart cloud-file-processing
sudo journalctl -u cloud-file-processing -n 100 --no-pager
```

## 12. Useful Verification Commands

Check service definition:

```bash
sudo systemctl cat cloud-file-processing
```

Check env file:

```bash
sudo cat /etc/cloud-file-processing/app.env
```

Check `systemctl` path:

```bash
which systemctl
```

Check current health:

```bash
curl -v http://localhost:8080/actuator/health
```

## 13. Optional Node Stability Checks

If Jenkins reports low disk, temp, or swap, remember:

- `Free Temp Space` is disk space on `/tmp`, not RAM.
- `Swap` is disk-backed virtual memory.

Check them:

```bash
df -h
df -h /tmp
free -h
swapon --show
```

Optional 1G swap file:

```bash
sudo fallocate -l 1G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

Optional 2G swap file:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

## 14. End-To-End Checklist

1. Jenkins can access the repository.
2. Maven test stage passes.
3. Maven package stage creates the jar.
4. EC2 has `/opt/cloud-file-processing/app.jar`.
5. `cloud-file-processing.service` exists and uses `EnvironmentFile=/etc/cloud-file-processing/app.env`.
6. `app.env` contains correct DB and AWS values.
7. Jenkins can run `sudo systemctl restart cloud-file-processing` without password.
8. `curl http://localhost:8080/actuator/health` returns `{"status":"UP"}`.
9. Upload API succeeds.
10. SQS queue URL is a plain URL and not prefixed with `sqs-queue-url:`.
