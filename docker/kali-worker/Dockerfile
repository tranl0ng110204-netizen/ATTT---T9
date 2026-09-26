FROM kalilinux/kali-rolling

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
    nmap \
    gobuster \
    dirb \
    curl \
    iputils-ping \
    nikto \
    sslscan \
    whatweb \
    wafw00f \
    dirsearch && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

CMD ["tail", "-f", "/dev/null"]