1. Install docker-compose (https://docs.docker.com/compose/install/)
2. Run:
  sudo sysctl -w vm.max_map_count=262144
  sudo docker-compose up
  echo "$(date +'%m-%d %H:%M:%S.000') 123 456 D LogName: message" | nc localhost 5000
  (CTRL+C)
3. Visit http://localhost:5601
4. Enter:
  Pattern: logstash-*
  Time-field name: @timestamp
  Create
5. Go to Discover
6. Send logs like this:
  echo "$(date +'%m-%d %H:%M:%S.000') 123 456 D LogName: message" | nc localhost 5000 
