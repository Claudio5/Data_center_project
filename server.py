from flask import Flask, request
from flask_restful import Resource, Api
from sqlalchemy import create_engine
from json import dumps
from flask.ext.jsonpify import jsonify

app = Flask(__name__)
api = Api(app)

class Racks(Resource):
    def get(self):
        # connect to database
        result= {'racks': ['rack01', 'rack02', 'rack03']}
        return jsonify(result)

class ServersInRack(Resource):
    def get(self, rack_name):

        if rack_name == 'rack01':
            result = {'servers': ['s01', 's02', 's03']}
        elif rack_name == 'rack02':
            result = {'servers': ['s01', 's02']}
        elif rack_name == 'rack03':
            result = {'servers': ['s01', 's02', 's03','s04']}
        else:
            result = 'error'
        return jsonify(result)

class CPUsInServer(Resource):
    def get(self,rack_name,server_name):
        if rack_name == 'rack01':
            if server_name == 's01':
                result = {'cpus': ['cpu01','cpu02']}

        return jsonify(result)

class ServerPowerLastSample(Resource):
    def get(self,rack_name,server_name):
        if rack_name == 'rack01':
            if server_name == 's01':
                result = {'s01': '50'}

        return jsonify(result)

class ServerPowerLast5min(Resource):
    def get(self,rack_name,server_name):
        if rack_name == 'rack01':
            if server_name == 's01':
                result = {'s01': ['10','5','7','15','18']}
            elif server_name == 's02':
                result = {'s02': ['1','2','3','4','5']}
            elif server_name == 's03':
                result = {'s03': ['5','4','3','2','1']}
        if rack_name == 'rack02':
            if server_name == 's01':
                result = {'s01': ['10','10','10','10','10']}
            elif server_name == 's02':
                result = {'s02': ['15','15','15','15','15']}
        elif rack_name == 'rack03' :
            if server_name == 's01' :
                result = {'s01':['20','19','18','17','16']}
            elif server_name == 's02':
                result = {'s02': ['5','5','5','5','5']}
            elif server_name == 's03':
                result = {'s03': ['10','11','12','11','10']}
            elif server_name == 's04':
                result = {'s04': ['0','10','0','10','0']}

        return jsonify(result)
class CPUPowerLast5min(Resource):
    def get(self,rack_name,server_name,cpu_name):
        if rack_name == 'rack01':
            if server_name == 's01':
                if cpu_name == 'cpu01':
                    result = {'cpu01': ['1','2','3','4','5']}

        return jsonify(result)

api.add_resource(Racks, '/racks') # Route_1
api.add_resource(ServersInRack, '/<rack_name>/servers') # Route_2
api.add_resource(CPUsInServer, '/<rack_name>/<server_name>/cpus') # Route_3
api.add_resource(ServerPowerLastSample,'/<rack_name>/<server_name>/power/last')
api.add_resource(ServerPowerLast5min,'/<rack_name>/<server_name>/power/last5min')
api.add_resource(CPUPowerLast5min,'/<rack_name>/<server_name>/<cpu_name>')

if __name__ == '__main__':
     app.run(host='128.179.198.237',port='5002')
