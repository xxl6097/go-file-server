#!/bin/bash

chown -R ${PUID}:${PGID} /app/

umask ${UMASK}

exec su-exec ${PUID}:${PGID} /app/main --conf /app/config.yml