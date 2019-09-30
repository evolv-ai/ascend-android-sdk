## [0.6.1-beta] - 2019-09-30
### Added
- confirmations/contaminations are now only sent when the allocation is used
- improved logging messages

## [0.6.0-beta] - 2019-05-31
### Fixed 
- Participant's now initialize with the client instead of getting reused like before.
- Stack traces are now logged upon exceptions.
### Added
- Added audience filters, allowing for filtering of the participant through user attributes.
- AllocationStore now requires a unique user id to be given with the allocation to be stored.