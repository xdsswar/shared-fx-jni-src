/**
 * @author XDSSWAR
 * Created on 09/30/2023
 */
module fx.jni {
    requires javafx.graphics;
    requires javafx.controls;





    exports com.sun.fx.jni to javafx.graphics, javafx.controls, javafx.fxml, javafx.base;
    opens com.sun.fx.jni to javafx.graphics, javafx.controls, javafx.fxml, javafx.base;

    exports xss.it.fx.helpers;
    opens xss.it.fx.helpers;
    exports xss.it.fx;
    opens xss.it.fx;
}