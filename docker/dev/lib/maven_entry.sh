#!/bin/bash
apt update 
apt install -y shellinabox zsh
echo "root:toor" | chpasswd
export CHSH=yes
echo Y | sh -c "$(wget -O- https://raw.githubusercontent.com/robbyrussell/oh-my-zsh/master/tools/install.sh)"
sed -i 's/bash/zsh/g' /etc/passwd
shellinaboxd --disable-ssl --css /black_on_white.css
