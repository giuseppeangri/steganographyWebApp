(function() {

    'use strict';

    angular
        .module('app', [
            'ngFileSaver'
        ])
        .controller('MainController', MainController)
        .factory('Server', Server);

    MainController.$inject = ['$scope', '$location', '$anchorScroll', 'Server', 'FileSaver', 'Blob'];

    function MainController($scope, $location, $anchorScroll, Server, FileSaver, Blob) {

        var mc = this;

        mc.$onInit       = initialize;
        mc.setMode       = setMode;
        mc.setFile       = setFile;
        mc.downloadImage = downloadImage;
        mc.downloadText  = downloadText;
        mc.toggleSection = toggleSection;
        mc.start         = start;

        return this;

        function initialize() {

            $scope.modes = [
                {
                    name : 'Encode',
                    icon : 'lock_outline'
                },
                {
                    name : 'Decode',
                    icon : 'lock_open'
                }
            ];

            $scope.algorithms = {
                F5 : {
                    encodeDescription : '(Output JPG)',
                    decodeDescription : '(Input JPG)',
                    encodeApi         : Server.encodeF5,
                    decodeApi         : Server.decodeF5,
                    imageExtension    : 'jpeg'
                },
                LSB : {
                    encodeDescription : '(Output PNG)',
                    decodeDescription : '(Input PNG)',
                    encodeApi         : Server.encodeLSB,
                    decodeApi         : Server.decodeLSB,
                    imageExtension    : 'png'
                }
            };

            $scope.selected = {
                mode             : 'Encode',
                encodeAlgorithms : {
                    F5  : true,
                    LSB : true
                },
                decodeAlgorithm : 'F5'
            };

            $scope.input          = {};
            $scope.sections       = {};
            $scope.compareSection = {};

        }

        function setMode(mode) {
            $scope.selected.mode = mode;
        }

        function setFile(type, file) {
            $scope.input[type] = file;
        }

        function toggleSection(section) {
            section.collapsed = !section.collapsed;
        }

        function reset() {
            $scope.sections       = {};
            $scope.compareSection = {};
        }

        function start() {

            reset();

            if($scope.selected.mode == 'Encode') {

                if($scope.input.coverImage && $scope.input.textFile && $scope.input.key) {

                    var formData = new FormData();

                    formData.append('image', $scope.input.coverImage);
                    formData.append('embed', $scope.input.textFile);
                    formData.append('key', $scope.input.key);

                    for(var selected in $scope.selected.encodeAlgorithms) {

                        if($scope.selected.encodeAlgorithms[selected]) {

                            launchEncode(selected, formData);

                        }
                        
                    }

                    if(Object.keys($scope.sections).length > 1) {

                        $scope.sections['compare'] = {
                            title   : 'Results Compare',
                            compare : true
                        }

                    }

                }
                else {
                    $scope.sections['error'] = {
                        title : 'Error',
                        error : 'Insert all fields to start'
                    };
                }

            }
            else if($scope.selected.mode == 'Decode') {

                if($scope.input.steganoImage && $scope.input.key) {

                    var formData = new FormData();

                    formData.append('image', $scope.input.steganoImage);
                    formData.append('key', $scope.input.key);

                    launchDecode($scope.selected.decodeAlgorithm, formData);

                }
                else {
                    $scope.sections['error'] = {
                        title : 'Error',
                        error : 'Insert all fields to start'
                    };
                }

            }
            
            $location.hash('sections');
            $anchorScroll();

        }

        function _arrayBufferToBase64(buffer) {

            var binary = '';
            var bytes  = new Uint8Array(buffer);
            var len    = bytes.byteLength;

            for(var i = 0; i < len; i++) {
                binary += String.fromCharCode(bytes[i]);
            }

            return window.btoa(binary);
        }

        function formatBytes(bytes,decimals) {
            if(bytes == 0) return '0 Bytes';
            var k = 1000,
                dm = decimals || 2,
                sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
                i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
        }

        function launchEncode(algorithmName, formData) {

            var algorithm = $scope.algorithms[algorithmName];

            var section = $scope.sections['encode'+algorithmName] = {
                algorithm : {
                    name           : algorithmName,
                    imageExtension : algorithm.imageExtension
                },
                title     : algorithmName+' Encode',
                loading   : true
            };

            algorithm.encodeApi(formData).then(

                function successCallback(response) {

                    section.loading = false;

                    section.image = {
                        extension : algorithm.imageExtension,
                        byteArray : response.data,
                        base64    : _arrayBufferToBase64(response.data),
                        src       : 'data:image/'+algorithm.imageExtension+';base64,'+_arrayBufferToBase64(response.data)
                    };

                    section.info = {
                        width     : response.headers('IMAGE_WIDTH'),
                        height    : response.headers('IMAGE_HEIGHT'),
                        size      : formatBytes(section.image.base64.length*0.75),
                        mse       : response.headers('MSE'),
                        peak      : response.headers('PEAK'),
                        psnr_peak : response.headers('PSNR_PEAK'),
                        psnr_255  : response.headers('PSNR_255')
                    };

                },

                function errorCallback(response) {

                    section.loading = false;
                    section.error   = response.headers('ERROR');

                }

            );

        }

        function launchDecode(algorithmName, formData) {

            var algorithm = $scope.algorithms[algorithmName];

            var section = $scope.sections['decode'+algorithmName] = {
                algorithm : {
                    name           : algorithmName,
                    imageExtension : algorithm.imageExtension
                },
                title     : algorithmName+' Decode',
                loading   : true
            };

            algorithm.decodeApi(formData).then(

                function successCallback(response) {

                    section.loading = false;

                    section.text = {
                        text : response.data,
                        size : formatBytes(response.data.length)
                    };

                },

                function errorCallback(response) {
                    section.loading = false;
                    section.error   = response.headers('ERROR');
                }

            );

        }

        function downloadImage(image) {

            var blob = new Blob([image.byteArray], { type: 'image/'+image.extension });

            FileSaver.saveAs(blob, 'image.'+image.extension);

        }

        function downloadText(text) {

            var blob = new Blob([text.text], { type: 'text/plain' });

            FileSaver.saveAs(blob, 'text.txt');

        }

    }

    Server.$inject = ['$http'];

    function Server($http) {

        var url = 'api/';

        var config = {
            method           : 'POST',
            headers          : {
                'Content-Type' : undefined
            },
            transformRequest : angular.identity
        }
        
        var actions = {
            encodeF5  : encodeF5,
            decodeF5  : decodeF5,
            encodeLSB : encodeLSB,
            decodeLSB : decodeLSB
        };

        return actions;

        function encodeF5(formData) {
            return $http(angular.extend({
                url          : url+'f5/encode',
                data         : formData,
                responseType : 'arraybuffer'
            }, config));
        }

        function decodeF5(formData) {
            return $http(angular.extend({
                url  : url+'f5/decode',
                data : formData
            }, config));
        }

        function encodeLSB(formData) {
            return $http(angular.extend({
                url          : url+'lsb/encode',
                data         : formData,
                responseType : 'arraybuffer'
            }, config));
        }

        function decodeLSB(formData) {
            return $http(angular.extend({
                url  : url+'lsb/decode',
                data : formData
            }, config));
        }

    }

})();