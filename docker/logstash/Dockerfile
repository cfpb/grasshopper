FROM logstash:1.5.4-1

ADD grasshopper.conf /opt/

RUN /opt/logstash/bin/plugin install --version 0.1.4 logstash-output-syslog

CMD ["logstash", "-f", "/opt/grasshopper.conf"]

