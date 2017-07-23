# Set up ftp server
sudo apt-get -y install pure-ftpd
sudo groupadd ftpgroup
sudo useradd ftpuser -g ftpgroup -s /sbin/nologin -d /dev/null
sudo mkdir ~/Public/ftp
sudo chown -R ftpuser:ftpgroup ~/Public/ftp
sudo pure-pw useradd upload -u ftpuser -g ftpgroup -d ~/Public/ftp -m
sudo pure-pw mkdb
sudo ln -s /etc/pure-ftpd/conf/PureDB /etc/pure-ftpd/auth/60puredb
sudo service pure-ftpd restart