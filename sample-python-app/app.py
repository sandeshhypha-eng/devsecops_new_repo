from flask import Flask, request, render_template_string, make_response
import os
import pickle

app = Flask(__name__)


@app.route('/')
def index():
    return 'Hello from vulnerable Python app'


@app.route('/cmd')
def cmd():
    # Command injection vulnerability: executing user-supplied input
    cmd = request.args.get('cmd', '')
    if not cmd:
        return 'pass a ?cmd=... to execute (vulnerable demo)'
    # Dangerous: executing unsanitized input
    result = os.popen(cmd).read()
    return '<pre>' + result + '</pre>'


@app.route('/xss')
def xss():
    name = request.args.get('name', 'guest')
    # vulnerable: rendering user input without escaping
    return render_template_string('<h1>Hello ' + name + '</h1>')


@app.route('/eval')
def do_eval():
    # Extremely dangerous: evaluating user input on the server
    expr = request.args.get('expr', '')
    if not expr:
        return 'pass ?expr= to evaluate (vulnerable demo)'
    # Vulnerability: arbitrary code execution via eval
    try:
        out = eval(expr)
        return '<pre>' + str(out) + '</pre>'
    except Exception as e:
        return f'Error: {e}'


@app.route('/pickle', methods=['POST'])
def do_pickle():
    # Vulnerability: insecure deserialization. Loading pickles from untrusted sources is unsafe.
    data = request.get_data()
    try:
        obj = pickle.loads(data)
        return 'Unpickled object: ' + str(obj)
    except Exception as e:
        return 'Unpickle error: ' + str(e)


@app.route('/upload', methods=['POST'])
def upload():
    # Unsafe file upload: we save file using provided filename without sanitization
    filename = request.args.get('filename', 'upload.bin')
    content = request.get_data()
    # Vulnerability: path traversal if filename contains ../
    path = '/tmp/uploads/' + filename
    with open(path, 'wb') as f:
        f.write(content)
    return 'Saved to ' + path


@app.route('/session-login')
def session_login():
    # Create a very simple session by setting a cookie (no HttpOnly/Secure flags)
    session_id = 'sess-' + os.urandom(8).hex()
    resp = make_response('Logged in (insecure cookie)')
    # Vulnerability: no HttpOnly or Secure - accessible to JS and not marked secure
    resp.set_cookie('SESSIONID', session_id)
    return resp


if __name__ == '__main__':
    # Hard-coded secret in code to be detected
    API_KEY = "APIKEY-12345-SECRET"
    print('Starting vulnerable Flask app (port 5000)')
    app.run(host='0.0.0.0', port=5000)
 

 API_KEY =  JFEJHLGEFJG234231HJJBH423423H23
 password = "P@ssw0rd123"efuewhfew
aws_secret_access_key = "AKIAIOSFODNN7EXAMvsafvPLE"
aws_session_token = "lc3Npb24vZXhhbXBsZS9hd3Mtc2Vzc2lvbi10b2tlbi9leGFtcGxlLWF3cy1zZXNzaW9uLXRva2VuLTIzNDI"