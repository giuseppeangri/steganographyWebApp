<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>SteganoApp</title>
</head>

<body>
		
		<h2>SteganoApp!</h2>
		
		<h3>F5 Encode</h3>
		<form name="formname" action="api/f5/encode" method="post" enctype="multipart/form-data" style="padding-left: 5%;">
			<h4>Image</h4>
			<input type="file" name="image" />
			<h4>Embed</h4>
			<input type="file" name="embed" />
			<h4>Key</h4>
			<input type="text" name="key" />
			<br/>
			<br/>
			<input type="submit" />
		</form>

		
		<h3>F5 Decode</h3>
		<form name="formname" action="api/f5/decode" method="post" enctype="multipart/form-data" style="padding-left: 5%;">
			<h4>Image</h4>
			<input type="file" name="image" />
			<h4>Key</h4>
			<input type="text" name="key" />
			<br/>
			<br/>
			<input type="submit" />
		</form>

		<h3>LSB Encode</h3>
		<form name="formname" action="api/lsb/encode" method="post" enctype="multipart/form-data" style="padding-left: 5%;">
			<h4>Image</h4>
			<input type="file" name="image" />
			<h4>Embed</h4>
			<input type="file" name="embed" />
			<h4>Key</h4>
			<input type="text" name="key" />
			<br/>
			<br/>
			<input type="submit" />
		</form>
		
		<h3>LSB Decode</h3>
		<form name="formname" action="api/lsb/decode" method="post" enctype="multipart/form-data" style="padding-left: 5%;">
			<h4>Image</h4>
			<input type="file" name="image" />
			<h4>Key</h4>
			<input type="text" name="key" />
			<br/>
			<br/>
			<input type="submit" />
		</form>
		
</body>

</html>