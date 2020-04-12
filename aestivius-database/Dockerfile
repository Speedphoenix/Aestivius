FROM node:alpine
WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install

COPY index.js ./

EXPOSE 8096
CMD [ "node", "index" ]
