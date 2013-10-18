COMPUTEX_DIR="computex"
GIT_COMPUTEX_REPO="https://github.com/weso/computex.git"
GIT_COMPUTEX_BRANCH="master"


function update_repo {
	if [[ -d "$1" && ! -L "$1" ]] ; then
		(cd $1; git checkout $3)
		(cd $1; git pull origin $3)
	else
		git clone $2
		(cd $1; git checkout $3)
	fi
}

update_repo ${COMPUTEX_DIR} ${GIT_COMPUTEX_REPO} ${GIT_COMPUTEX_BRANCH}

#Starts Play Project
if [ -e ${COMPUTEX_DIR}/RUNNING_PID ] ; then 
	echo 'Computex is actually running. You must stopped it before.'
else 
	(cd ${COMPUTEX_DIR}; play clean compile stage; play "start 9003")
fi
