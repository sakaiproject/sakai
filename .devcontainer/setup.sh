chmod +x /usr/local/sdkman/candidates/tomcat/current/bin/*.sh
sudo chown vscode.vscode ~/.m2

mkdir -p /home/vscode/sakai-tomcat/logs

ln -s /workspaces/app/.devcontainer/tomcat/bin /home/vscode/sakai-tomcat/bin
ln -s /workspaces/app/.devcontainer/tomcat/conf /home/vscode/sakai-tomcat/conf
ln -s /workspaces/app/.devcontainer/tomcat/sakai-$1 /home/vscode/sakai-tomcat/sakai

cat >> ~/.bash_aliases <<EOF
alias mcid="mvn clean install sakai:deploy-exploded -DskipTests=true -P$1"
alias tlog='tail -f $CATALINA_BASE/logs/catalina.out'
alias tstart='catalina.sh start'
alias tstop='catalina.sh stop'
alias olog='code $CATALINA_BASE/logs/catalina.out'
alias rlog='rm -rf $CATALINA_BASE/logs/*'
alias tremove='rm -rf $CATALINA_BASE/lib $CATALINA_BASE/components $CATALINA_BASE/webapps'
EOF

sudo apt-get update && sudo apt-get install -y fontconfig fonts-dejavu fonts-liberation && sudo fc-cache -f -v
