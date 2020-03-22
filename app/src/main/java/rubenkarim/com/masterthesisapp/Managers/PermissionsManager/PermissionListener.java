package rubenkarim.com.masterthesisapp.Managers.PermissionsManager;

public interface PermissionListener {
    void permissionGranted(String[] permissions);
    void permissionDenied(String[] permissions);
}
