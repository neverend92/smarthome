var getStatusLine = function (type, msg) {
    var ret = '<span class="text-';
    
    switch (type) {
        case 'ok':
            ret += 'success';
            break;
        case 'error':
            ret += 'danger';
            break;
        case 'warn':
            ret += 'warning';
            break;
        case 'info':
        default:
            ret += 'info';
            break;
    }
    
    ret += '">[';
    ret += type.toUpperCase();
    ret += ']&nbsp;';
    
    for (var i = type.length; i < 5; i++) {
        console.log(ret);
        ret += '&nbsp;';
    }
    
    ret += msg;
    ret += '</span><br>';
    
    return ret;
};

var getIP = function () {
    return $('input[name="ip"]').val();
};

var getCredentials = function () {
    var creds = $('input[name="credentials"]').val();
    var a_tmp_creds = creds.split(':');
    var a_creds = ['', ''];
    if (a_tmp_creds.length == 2) {
        a_creds[0] = a_tmp_creds[0];
        a_creds[1] = a_tmp_creds[1];
    }
    
    return a_creds;
};

var callData = function (firstCall) {
    $('#p-info').html(getStatusLine('info', 'loading...'));
    var ip = getIP();
    var a_creds = getCredentials();
    $.ajax({
        url: ip + '/rest/auth',
        method: 'POST',
        data: {
            username: a_creds[0],
            password: a_creds[1]
        },
        success: function (res) {
            var html = '';
            if (!firstCall) {
                html += getStatusLine('warn', 'changes may not be saved yet!');
            }
            html += getStatusLine('ok', 'node is online');
            html += getStatusLine('ok', 'credentials are valid');
            $('#p-info').html(html);
            
            if (!res.token) {
                html = $('#p-info').html();
                html += getStatusLine('error', 'something went wrong');
                $('#p-info').html(html);
                return;
            }
            
            callVersion(res.token);
            callGetItems(res.token);
        },
        error: function (xhr, status, error) {
            var html = '';
            if (!firstCall) {
                html += getStatusLine('warn', 'changes may not be saved yet.');
            }
            
            if (xhr.status && xhr.status == 401) {
                html += getStatusLine('ok', 'node is online');
                html += getStatusLine('error', 'credentials are invalid');
            } else {
                html += getStatusLine('error', 'node is offline');
            }
            
            $('#p-info').html(html);
        }
    });
};

var callVersion = function (token) {
    var ip = getIP();
    $.ajax({
        url: ip + '/rest?api_key='+token,
        method: 'GET',
        success: function (res) {
            var html = '';
            if (!res.version) {
                html = $('#p-info').html();
                html += getStatusLine('error', 'could not obtain version');
                $('#p-info').html(html);
                return;
            }
            
            html = $('#p-info').html();
            html += getStatusLine('info', 'node uses Eclipse Smarthome ' + res.version);
            html += getStatusLine('warn', 'check extensions / bindings. NOT IMPLEMENTED YET.');
            $('#p-info').html(html);
        },
        error: function (xhr, status, error) {
            var html = $('#p-info').html();
            html += getStatusLine('error', 'could not obtain version');
            $('#p-info').html(html);
        }
    });    
};

var callGetItems = function (token) {
    var ip = getIP();
    $.ajax({
        url: ip + '/rest/items?recursive=false&api_key='+token,
        method: 'GET',
        success: function (res) {
            var data = '';
            for (var i = 0; i < res.length; i++) {
                data += '<tr>';
                data += '<td><a href="' + res[i].link + '?api_key=' + token + '" target="_blank">' + res[i].name + '</a></td>';
                data += '<td>' + res[i].label + '</td>';
                data += '<td>' + res[i].category + '</td>';
                data += '<td>' + res[i].type + '</td>';
                data += '<td>';
                data += '<a class="btn btn-warning" href="#">Edit Item</a>';
                data += '<a class="btn btn-danger" href="#">Delete Item</a>';
                data += '</td>';
                data += '</tr>';
            }
            $('#table-items').html(data);
            
            var html = '';
            html = $('#p-info').html();
            html += getStatusLine('info', 'loaded items from node');
            $('#p-info').html(html);
        },
        error: function (xhr, status, error) {
            var html = $('#p-info').html();
            html += getStatusLine('error', 'could not obtain items');
            $('#p-info').html(html);
        }
    });  
}

$(document).ready(function () {
    $('#btn-check-node').click(function () {
        callData(false);
    });
    callData(true);
});