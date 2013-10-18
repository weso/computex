COMPUTEX_DIR="computex"

kill -9 $(cat ${COMPUTEX_DIR}/RUNNING_PID)
rm ${COMPUTEX_DIR}/RUNNING_PID
if [ $? == 0 ] ; then
    echo 'Computex has stopped correctly'
else
    echo 'Computex cannot be stopped. See log to view details.'
fi 
	


