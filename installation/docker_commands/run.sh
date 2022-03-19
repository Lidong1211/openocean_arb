#!/bin/bash
# init
# =============================================
# SCRIPT COMMANDS
echo
echo "===============  START OPENOCEAN_BOT INSTANCE ==============="
echo
echo "List of all docker instances:"
echo
docker ps -a
echo
echo
read -p "   Enter the NAME of the OpenOceanBot instance to start or connect to (default = \"openoceanbot-instance\") >>> " INSTANCE_NAME
if [ "$INSTANCE_NAME" == "" ]
then
  INSTANCE_NAME="openoceanbot-instance"
fi
echo
# =============================================
# EXECUTE SCRIPT
docker start $INSTANCE_NAME && docker attach $INSTANCE_NAME
