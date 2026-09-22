FROM ubuntu:latest
LABEL authors="jerzymaj"

ENTRYPOINT ["top", "-b"]