#!/bin/bash
# File: create.sh

echo
echo
echo "===============  CREATE A NEW OPENOCEAN_BOT INSTANCE ==============="
echo
echo
echo "    ℹ️  Press [ENTER] for default values:"
echo

# Specify openoceanbot version
read -p "   Enter OpenOceanBot version you want to use [latest/development] (default = \"latest\") >>> " TAG
if [ "${TAG}" == "" ]; then
  TAG="latest"
fi

# Ask the user for the name of the new instance
read -p "   Enter a name for your new OpenOceanBot instance (default = \"openoceanbot-instance\") >>> " INSTANCE_NAME
if [ "$INSTANCE_NAME" == "" ]; then
  INSTANCE_NAME="openoceanbot-instance"
fi

prompt_proceed() {
  read -p "   Do you want to proceed? [Y/N] >>> " PROCEED
  if [ "$PROCEED" == "" ]; then
    PROCEED="Y"
  fi
}

# Execute docker commands
create_instance() {
  echo
  echo "Creating OpenOceanBot instance ... Admin password may be required to set the required permissions ..."
  echo
  # Launch a new instance of openoceanbot
  docker build -t openocean/openoceanbot:$TAG .
}

run_instance() {
  docker run -it --name $INSTANCE_NAME openocean/openoceanbot:$TAG
}

prompt_proceed
echo `create_instance`
if [[ "$PROCEED" == "Y" || "$PROCEED" == "y" ]]; then
  run_instance
else
  echo "   Aborted"
  echo
fi
